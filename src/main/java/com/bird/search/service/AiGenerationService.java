package com.bird.search.service;


import com.bird.search.dto.AttributeGenerationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Buffer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.bedrockruntime.model.InferenceConfiguration;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseRequest;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.Message;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiGenerationService {


  private final ObjectMapper mapper = new ObjectMapper();
  private final BedrockRuntimeClient bedrockClient;
  private final ResourceLoader resourceLoader;

  @Value("${aws.bedrock.generation-model-id}")
  private String generationModelId;

  private String promptTemplate; // Cached prompt template


  /**
   * Lazy-loads the prompt template from external file.
   */
  private String getPromptTemplate(){
    if(promptTemplate == null) {
      try{
        Resource resource = resourceLoader.getResource("classpath:prompts/generation-attribute-prompt.txt");
        try(BufferedReader reader = new BufferedReader(
            new InputStreamReader(resource.getInputStream())
        )) {
          promptTemplate = reader.lines().collect(Collectors.joining("\n"));
        }
        log.info("Loaded prompt template from classpath:prompts/generation-attribute-prompt.txt");
        } catch (Exception e) {
          log.error("Failed to load prompt template", e);
          throw new RuntimeException("Could not load prompt template", e);
        }
    }
    return promptTemplate;
  }



  /**
   * Generates a full attribute payload from a partial natural-language description.
   *
   * <p>The method sends the prompt to Bedrock Converse, sanitizes the textual output,
   * and deserializes the resulting JSON into {@link AttributeGenerationResponse}.</p>
   *
   * @param partialDescription user-provided description used as generation context
   * @return structured attribute generation response
   * @throws RuntimeException if the model output is empty or cannot be parsed as valid JSON
   */
  public AttributeGenerationResponse generateAttribute(String partialDescription){
    if (partialDescription == null || partialDescription.isBlank()) {
      throw new IllegalArgumentException("partialDescription must not be blank");
    }

    String prompt = getPromptTemplate().formatted(partialDescription);

    ConverseRequest request = ConverseRequest.builder()
        .modelId(generationModelId)
        .messages(Message.builder()
            .role("user")
            .content(ContentBlock.builder().text(prompt).build())
            .build())
        .inferenceConfig(InferenceConfiguration.builder()
            .maxTokens(1024)
            .temperature(0.2f)
            .build())
        .build();

    ConverseResponse response = bedrockClient.converse(request);

    String aiRaw = extractAssistantText(response);
    String aiJson = sanitizeJsonPayload(aiRaw);

    try {
      return mapper.readValue(aiJson, AttributeGenerationResponse.class);
    } catch (Exception e) {
      log.error("AI payload cannot be parsed as JSON. raw='{}'", abbreviate(aiRaw, 1200));
      throw new RuntimeException("Invalid AI JSON from Converse API", e);
    }
  }

  /**
   * Removes markdown code fences and trims surrounding non-JSON text.
   *
   * <p>This protects JSON deserialization when a model wraps the payload in
   * ```json blocks or adds short prose before/after the JSON object.</p>
   *
   * @param raw raw model text output
   * @return sanitized JSON string ready for Jackson parsing
   */
  private String sanitizeJsonPayload(String raw) {
    String s = raw == null ? "" : raw.trim();

    // Remove fenced markdown blocks: ```json ... ``` or ``` ... ```
    if (s.startsWith("```")) {
      int firstNewLine = s.indexOf('\n');
      if (firstNewLine > -1) {
        s = s.substring(firstNewLine + 1).trim();
      }
      if (s.endsWith("```")) {
        s = s.substring(0, s.length() - 3).trim();
      }
    }

    // Keep first JSON object if extra prose is present.
    int start = s.indexOf('{');
    int end = s.lastIndexOf('}');
    if (start >= 0 && end > start) {
      s = s.substring(start, end + 1);
    }

    return s;
  }


    private String extractAssistantText(ConverseResponse response) {
      if (response == null || response.output() == null || response.output().message() == null) {
        throw new IllegalStateException("Converse response has no output/message");
      }

      List<ContentBlock> blocks = response.output().message().content();
      if (blocks == null || blocks.isEmpty()) {
        throw new IllegalStateException("Converse response has empty content blocks");
      }

      for (ContentBlock block : blocks) {
        String text = block.text();
        if (text != null && !text.isBlank()) {
          return text;
        }
      }

      String rawBlocks = safeToJson(blocks);
      throw new IllegalStateException("No text block found in Converse response. blocks=" + abbreviate(rawBlocks, 1200));
    }

    private String safeToJson(Object value) {
      try {
        return mapper.writeValueAsString(value);
      } catch (Exception e) {
        return String.valueOf(value);
      }
    }

    private String abbreviate(String value, int maxLen) {
      if (value == null) return "null";
      return value.length() <= maxLen ? value : value.substring(0, maxLen) + "...(truncated)";
    }


}

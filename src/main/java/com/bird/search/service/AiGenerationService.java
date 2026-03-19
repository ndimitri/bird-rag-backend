package com.bird.search.service;


import com.bird.search.config.AwsConfig;
import com.bird.search.dto.AttributeGenerationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

@Service
@RequiredArgsConstructor
public class AiGenerationService {


  private final ObjectMapper mapper = new ObjectMapper();
  private final BedrockRuntimeClient bedrockClient;

  public AttributeGenerationResponse generateAttribute(String partialDescription){
    String prompt = """
            You are an assistant that generates metadata for variables in a data governance system.

            Based on this partial description: "%s"

            Generate a JSON object with the following structure:

            {
              "metadata": {
                "entity_type": "variable",
                "maintenance_agency": "BIRD",
                "id": "...",
                "code": "...",
                "title": "...",
                "domain_id": "...",
                "parent_variable_id": null,
                "valid_from": "YYYY-MM-DD",
                "valid_to": "9999-12-31",
                "description": "..."
              }
            }

            Rules:
            - id and code must be UPPERCASE_WITH_UNDERSCORES
            - title must be in English
            - domain_id must be coherent with the description
            - valid_from must be today's date
            - Respond STRICTLY in JSON.
            """.formatted(partialDescription);

    String body = """
            {
              "anthropic_version": "bedrock-2023-05-31",
              "max_tokens": 1024,
              "messages": [
                {
                  "role": "user",
                  "content": "%s"
                }
              ]
            }
            """.formatted(prompt);

    InvokeModelRequest request = InvokeModelRequest.builder()
        .modelId("anthropic.claude-3-sonnet-20240229-v1:0")
        .contentType("application/json")
        .accept("application/json")
        .body(SdkBytes.fromUtf8String(body))
        .build();

    InvokeModelResponse response = bedrockClient.invokeModel(request);

    String raw = response.body().asUtf8String();

    // Claude renvoie un JSON avec un champ "content" → il faut l'extraire
    try {
      JsonNode root = mapper.readTree(raw);
      String aiJson = root.get("content").get(0).get("text").asText();
      return mapper.readValue(aiJson, AttributeGenerationResponse.class);
    } catch (Exception e) {
      throw new RuntimeException("Invalid AI JSON: " + raw, e);
    }

  }






}

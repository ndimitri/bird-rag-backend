package com.bird.search.controller;

import com.bird.search.dto.AttributeGenerationRequest;
import com.bird.search.dto.AttributeGenerationResponse;
import com.bird.search.service.AiGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for AI-powered attribute generation.
 *
 * <p>Exposes endpoints to generate metadata attributes using AWS Bedrock models
 * based on partial natural-language descriptions.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai-generation")
public class AiGenerationController {

  private final AiGenerationService aiGenerationService;

  /**
   * Generates a full attribute from a partial description.
   *
   * @param request contains the partial description text
   * @return full attribute generation response with metadata
   */
  @PostMapping("/generate-attribute")
  public AttributeGenerationResponse generateAttribute(@RequestBody AttributeGenerationRequest request) {
    return aiGenerationService.generateAttribute(request.getPartialDescription());
  }

}

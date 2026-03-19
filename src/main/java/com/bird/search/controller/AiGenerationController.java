package com.bird.search.controller;

import com.bird.search.dto.AttributeGenerationRequest;
import com.bird.search.dto.AttributeGenerationResponse;
import com.bird.search.service.AiGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai-generation")
public class AiGenerationController {

  private final AiGenerationService aiGenerationService;


  @PostMapping("/generate-attribute")
  public AttributeGenerationResponse generateAttribute(@RequestBody AttributeGenerationRequest request) {
    return aiGenerationService.generateAttribute(request.getPartialDescription());
  }


}

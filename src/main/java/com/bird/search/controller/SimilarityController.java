//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.bird.search.controller;

import com.bird.search.dto.RetrievedResult;
import com.bird.search.service.BedrockSimilarityService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/similarity"})
public class SimilarityController {
  private final BedrockSimilarityService service;

  @GetMapping({"/search"})
  public ResponseEntity<List<RetrievedResult>> search(@RequestParam String query) {
    List<RetrievedResult> results = this.service.search(query);
    return ResponseEntity.ok(results);
  }

  public SimilarityController(final BedrockSimilarityService service) {
    this.service = service;
  }
}

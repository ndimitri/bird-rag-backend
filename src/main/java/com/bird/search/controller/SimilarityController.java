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

/**
 * REST controller for semantic similarity and document retrieval.
 *
 * <p>Exposes endpoints to search a Bedrock Knowledge Base using vector search
 * and reranking to retrieve the most relevant documents.</p>
 */
@RestController
@RequestMapping("/api/similarity")
@RequiredArgsConstructor
public class SimilarityController {

  private final BedrockSimilarityService service;

  /**
   * Searches the Knowledge Base for documents matching the query.
   *
   * @param query natural-language search query
   * @return list of ranked retrieval results with metadata
   */
  @GetMapping("/search")
  public ResponseEntity<List<RetrievedResult>> search(@RequestParam String query) {
    List<RetrievedResult> results = service.search(query);
    return ResponseEntity.ok(results);
  }

}

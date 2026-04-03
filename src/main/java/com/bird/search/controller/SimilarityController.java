//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.bird.search.controller;

import com.bird.search.dto.AttributeSearchResult;
import com.bird.search.dto.MetadataSearchRequest;
import com.bird.search.service.BedrockSimilarityService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

  /*@GetMapping("/search")
  public ResponseEntity<List<RetrievedResult>> search(@RequestParam String query) {
    List<RetrievedResult> results = service.search(query);
    return ResponseEntity.ok(results);
  }*/


  @PostMapping("/search/filter")
  public List<AttributeSearchResult> searchWithMetadataFilters(@RequestBody MetadataSearchRequest request) {
    return service.searchWithDynamicMetadataFilters(request.getQuery(), request.getFilters());


  }

}

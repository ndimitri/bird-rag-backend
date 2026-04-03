package com.bird.search.service;

import com.bird.search.dto.AttributeSearchResult;
import com.bird.search.utils.BedrockFilterUtils;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseQuery;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseRetrievalConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseVectorSearchConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveRequest;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveResponse;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrievalFilter;
import software.amazon.awssdk.services.bedrockagentruntime.model.VectorSearchBedrockRerankingConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.VectorSearchBedrockRerankingModelConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.VectorSearchRerankingConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.VectorSearchRerankingConfigurationType;

/**
 * Service responsible for semantic retrieval against an AWS Bedrock Knowledge Base.
 *
 * <p>The retrieval pipeline performs vector search and applies Bedrock reranking
 * to improve relevance of the final returned documents.</p>
 */
@Service
@RequiredArgsConstructor
public class BedrockSimilarityService {

  @Value("${aws.bedrock.knowledge-base-id}")
  private String knowledgeBaseId;

  @Value("${aws.bedrock.reranking-model-arn}")
  private String rerankingModelArn;

  private static final int MAX_RESULTS = 100;
  private static final int MAX_RERANKING_RESULT = 5;

  private final BedrockAgentRuntimeClient bedrockAgentRuntimeClient;
  private final AttributeService attributeService;

  /**
   * Performs retrieval with dynamic metadata filters and maps results to attribute DTOs.
   *
   * @param query free-text query
   * @param filters metadata constraints provided by the caller
   * @return ranked attributes that match retrieval output
   */
  public List<AttributeSearchResult> searchWithDynamicMetadataFilters(
      String query,
      Map<String, String> filters
  ) {
    RetrievalFilter retrievalFilter = BedrockFilterUtils.buildAndFilter(filters);

    RetrieveRequest request = RetrieveRequest.builder()
        .knowledgeBaseId(knowledgeBaseId)
        .retrievalQuery(KnowledgeBaseQuery.builder().text(query).build())
        .retrievalConfiguration(KnowledgeBaseRetrievalConfiguration.builder()
            .vectorSearchConfiguration(KnowledgeBaseVectorSearchConfiguration.builder()
                .numberOfResults(MAX_RESULTS)
                .filter(retrievalFilter)
                .rerankingConfiguration(VectorSearchRerankingConfiguration.builder()
                    .type(VectorSearchRerankingConfigurationType.BEDROCK_RERANKING_MODEL)
                    .bedrockRerankingConfiguration(
                        VectorSearchBedrockRerankingConfiguration.builder()
                            .modelConfiguration(
                                VectorSearchBedrockRerankingModelConfiguration.builder()
                                    .modelArn(rerankingModelArn)
                                    .build())
                            .numberOfRerankedResults(MAX_RERANKING_RESULT)
                            .build())
                    .build())
                .build())
            .build())
        .build();

    RetrieveResponse response = bedrockAgentRuntimeClient.retrieve(request);

    return response.retrievalResults().stream()
        .map(result -> {
          String code = BedrockFilterUtils.extractCode(result.content().text());
          if (code == null) {
            return null;
          }

          return attributeService.getByCode(code)
              .map(v -> AttributeSearchResult.fromAttribute(v, result.score()))
              .orElse(null);
        })
        .filter(Objects::nonNull)
        .toList();
  }
}

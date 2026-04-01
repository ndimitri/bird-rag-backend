//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.bird.search.service;

import com.bird.search.dto.AttributeSearchResult;
import com.bird.search.dto.KbDocument;
import com.bird.search.dto.RetrievedResult;
import com.bird.search.utils.KbContentParser;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.document.Document;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
//import software.amazon.awssdk.services.bedrockagentruntime.model.BedrockRerankingConfiguration;
//import software.amazon.awssdk.services.bedrockagentruntime.model.BedrockRerankingModelConfiguration;
//import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseQuery;
//import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseRetrievalConfiguration;
//import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseVectorSearchConfiguration;
//import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveRequest;
//import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveResponse;
//import software.amazon.awssdk.services.bedrockagentruntime.model.VectorSearchRerankingConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.*;

@Service
@RequiredArgsConstructor
/**
 * Service responsible for semantic retrieval against an AWS Bedrock Knowledge Base.
 *
 * <p>The retrieval pipeline performs vector search and applies Bedrock reranking
 * to improve relevance of the final returned documents.</p>
 */
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
   * Searches the configured Knowledge Base with a natural-language query.
   *
   * <p>The method first retrieves vector candidates, then applies reranking using
   * the configured Bedrock reranking model, and finally maps results to
   * {@link RetrievedResult} DTOs.</p>
   *
   * @param query end-user search query
   * @return ranked list of retrieved results enriched with parsed metadata
   */
  public List<RetrievedResult> search(String query) {

//    Metthod without reranking
//    RetrieveRequest request = (RetrieveRequest)RetrieveRequest.builder()
//        .knowledgeBaseId(this.knowledgeBaseId)
//        .retrievalQuery((KnowledgeBaseQuery)KnowledgeBaseQuery
//            .builder()
//            .text(query)
//            .build())
//        .retrievalConfiguration((KnowledgeBaseRetrievalConfiguration)KnowledgeBaseRetrievalConfiguration.builder()
//            .vectorSearchConfiguration((KnowledgeBaseVectorSearchConfiguration)KnowledgeBaseVectorSearchConfiguration
//                .builder()
//                .numberOfResults(MAX_RESULTS)
//                .build())
//            .build())
//        .build();


    RetrieveRequest request = RetrieveRequest.builder()
        .knowledgeBaseId(knowledgeBaseId)
        .retrievalQuery(KnowledgeBaseQuery.builder()
            .text(query)
            .build())
        .retrievalConfiguration(KnowledgeBaseRetrievalConfiguration.builder()
            .vectorSearchConfiguration(KnowledgeBaseVectorSearchConfiguration.builder()
                .numberOfResults(MAX_RESULTS) // candidats avant reranking
                .rerankingConfiguration(VectorSearchRerankingConfiguration.builder()
                    .type(VectorSearchRerankingConfigurationType.BEDROCK_RERANKING_MODEL)
                    .bedrockRerankingConfiguration(
                        VectorSearchBedrockRerankingConfiguration.builder()
                            .modelConfiguration(
                                VectorSearchBedrockRerankingModelConfiguration.builder()
                                    .modelArn(rerankingModelArn)
                                    .build())
                            .numberOfRerankedResults(MAX_RERANKING_RESULT) // top N final
                            .build())
                    .build())
                .build())
            .build())
        .build();

    RetrieveResponse response = this.bedrockAgentRuntimeClient.retrieve(request);

    return response.retrievalResults().stream().map((result) -> {
      KbDocument doc = KbContentParser.parse(result.content().text());
      return new RetrievedResult(doc.text(), result.score(), doc.metadata());
    }).toList();
  }


  public List<AttributeSearchResult> searchWithDynamicMetadataFilters(
      String query,
      Map<String, String> filters
  ) {

    RetrievalFilter retrievalFilter = null;

    if (filters != null && !filters.isEmpty()) {

      List<RetrievalFilter> filterList = filters.entrySet().stream()
          .map(entry -> buildFilter(entry.getKey(), entry.getValue()))
          .filter(Objects::nonNull)
          .toList();

      if (!filterList.isEmpty()) {
        if (filterList.size() == 1) {
          retrievalFilter = filterList.getFirst();
        } else {
          retrievalFilter = RetrievalFilter.builder()
              .andAll(filterList)
              .build();
        }
      }
    }

    RetrieveRequest request = RetrieveRequest.builder()
        .knowledgeBaseId("PVLJFBKHFQ")
        .retrievalQuery(
            KnowledgeBaseQuery.builder()
                .text(query)
                .build()
        )
        .retrievalConfiguration(
            KnowledgeBaseRetrievalConfiguration.builder()
                .vectorSearchConfiguration(
                    KnowledgeBaseVectorSearchConfiguration.builder()
                        .numberOfResults(MAX_RESULTS)
                        .filter(retrievalFilter)
                        .rerankingConfiguration(
                            VectorSearchRerankingConfiguration.builder()
                                .type(VectorSearchRerankingConfigurationType.BEDROCK_RERANKING_MODEL)
                                .bedrockRerankingConfiguration(
                                    VectorSearchBedrockRerankingConfiguration.builder()
                                        .modelConfiguration(
                                            VectorSearchBedrockRerankingModelConfiguration.builder()
                                                .modelArn(rerankingModelArn)
                                                .build()
                                        )
                                        .numberOfRerankedResults(MAX_RERANKING_RESULT)
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build()
        )
        .build();

    RetrieveResponse response = bedrockAgentRuntimeClient.retrieve(request);

    return response.retrievalResults().stream()
        .map(result -> {
          String text = result.content().text();

          String code = extractCode(text);
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


  //region Utils
  private String extractCode(String text) {
    for (String line : text.split("\n")) {
      if (line.startsWith("CODE:")) {
        return line.substring("CODE:".length()).trim();
      }
    }
    return null;
  }

  private RetrievalFilter buildFilter(String key, String rawValue) {

    if (rawValue == null || rawValue.isBlank()) {
      return null;
    }

    rawValue = rawValue.trim();

    // >=
    if (rawValue.startsWith(">=")) {
      return RetrievalFilter.builder()
          .greaterThanOrEquals(
              FilterAttribute.builder()
                  .key(key)
                  .value(Document.fromNumber(
                      Long.parseLong(rawValue.substring(2))
                  ))
                  .build()
          )
          .build();
    }

    // <=
    if (rawValue.startsWith("<=")) {
      return RetrievalFilter.builder()
          .lessThanOrEquals(
              FilterAttribute.builder()
                  .key(key)
                  .value(Document.fromNumber(
                      Long.parseLong(rawValue.substring(2))
                  ))
                  .build()
          )
          .build();
    }

    // >
    if (rawValue.startsWith(">")) {
      return RetrievalFilter.builder()
          .greaterThan(
              FilterAttribute.builder()
                  .key(key)
                  .value(Document.fromNumber(
                      Long.parseLong(rawValue.substring(1))
                  ))
                  .build()
          )
          .build();
    }

    // <
    if (rawValue.startsWith("<")) {
      return RetrievalFilter.builder()
          .lessThan(
              FilterAttribute.builder()
                  .key(key)
                  .value(Document.fromNumber(
                      Long.parseLong(rawValue.substring(1))
                  ))
                  .build()
          )
          .build();
    }

    // = (égalité implicite)
    return RetrievalFilter.builder()
        .equalsValue(
            FilterAttribute.builder()
                .key(key)
                .value(Document.fromString(rawValue))
                .build()
        )
        .build();
  }


  //endregion



}

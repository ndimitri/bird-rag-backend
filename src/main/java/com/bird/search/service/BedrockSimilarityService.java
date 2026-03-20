//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.bird.search.service;

import com.bird.search.dto.KbDocument;
import com.bird.search.dto.RetrievedResult;
import com.bird.search.utils.KbContentParser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
  private static final int MAX_RESULTS = 10;
  private static final int MAX_RERANKING_RESULT = 5;
  private final BedrockAgentRuntimeClient bedrockAgentRuntimeClient;

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

}

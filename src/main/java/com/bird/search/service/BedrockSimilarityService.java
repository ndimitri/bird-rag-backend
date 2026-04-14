package com.bird.search.service;

import com.bird.search.dto.AttributeSearchResult;
import com.bird.search.dto.LegalDocumentSearchResult;
import com.bird.search.utils.BedrockFilterUtils;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.document.Document;
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

  @Value("${aws.bedrock.legal-document-knowledge-base-id}")
  private String legalDocumentKnowledgeBaseId;

  @Value("${aws.bedrock.reranking-model-arn}")
  private String rerankingModelArn;

  private static final int MAX_RESULTS = 100;
  private static final int MAX_RERANKING_RESULT = 5;
  private static final int LEGAL_DOCUMENTS_MAX_RERANKING_RESULT = 10;

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
          String code = result.metadata() != null && result.metadata().containsKey("CODE")
              ? result.metadata().get("CODE").asString()
              : null;
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



  public List<LegalDocumentSearchResult> searchLegalDocumentsWithDynamicMetadataFilters(
      String query,
      Map<String, String> filters
  ) {

    RetrievalFilter retrievalFilter = BedrockFilterUtils.buildAndFilter(filters);

    RetrieveRequest request = RetrieveRequest.builder()
        .knowledgeBaseId(legalDocumentKnowledgeBaseId)
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
                                        .numberOfRerankedResults(LEGAL_DOCUMENTS_MAX_RERANKING_RESULT)
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

          // --- sécurité minimale ---
          if (result.location() == null || result.location().s3Location() == null) {
            return null;
          }

          // --- infos de base ---
          String text = result.content() != null
              ? result.content().text()
              : null;

          String s3Uri = result.location().s3Location().uri();
          Double score = result.score();

          // --- titre (fallback = nom de fichier S3) ---
          String title;
          int lastSlash = s3Uri.lastIndexOf('/');
          title = lastSlash != -1 ? s3Uri.substring(lastSlash + 1) : s3Uri;

          // --- metadata Bedrock ---
          Map<String, Document> m = result.metadata();

          String entityType = m != null && m.containsKey("entity_type")
              ? m.get("entity_type").asString()
              : null;

          String documentType = m != null && m.containsKey("document_type")
              ? m.get("document_type").asString()
              : null;

          String regulation = m != null && m.containsKey("regulation")
              ? m.get("regulation").asString()
              : null;

          String article = m != null && m.containsKey("article")
              ? m.get("article").asString()
              : null;

          String attachedVariable = m != null && m.containsKey("attached_variable")
              ? m.get("attached_variable").asString()
              : null;

          String jurisdiction = m != null && m.containsKey("jurisdiction")
              ? m.get("jurisdiction").asString()
              : null;

          String validFrom = m != null && m.containsKey("valid_from")
              ? m.get("valid_from").asString()
              : null;

          String sourceUrl = m != null && m.containsKey("source_url")
              ? m.get("source_url").asString()
              : null;

          Integer pageNumber = m != null && m.containsKey("x-amz-bedrock-kb-document-page-number")
              ? m.get("x-amz-bedrock-kb-document-page-number").asNumber().intValue()
              : null;

          // --- construction DTO ---
          return new LegalDocumentSearchResult(
              title,
              text,
              s3Uri,
              score,
              entityType,
              documentType,
              regulation,
              article,
              attachedVariable,
              jurisdiction,
              validFrom,
              sourceUrl,
              pageNumber
          );
        })
        .filter(Objects::nonNull)
        .toList();
  }

}

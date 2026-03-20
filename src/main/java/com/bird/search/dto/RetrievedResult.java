//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.bird.search.dto;

import java.util.Map;

/**
 * Record for a retrieved document from semantic search.
 *
 * <p>Represents a ranked document result returned after Knowledge Base retrieval
 * and reranking, including relevance score and parsed metadata.</p>
 *
 * @param text the document text content
 * @param score relevance score assigned by the reranking model
 * @param metadata key-value pairs of document metadata
 */
public record RetrievedResult(
    String text,
    double score,
    Map<String, Object> metadata) {
}

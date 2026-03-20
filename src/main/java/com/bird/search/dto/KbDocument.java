//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.bird.search.dto;

import java.util.Map;

/**
 * Record for a Bedrock Knowledge Base document.
 *
 * <p>Represents a document retrieved from the Knowledge Base containing
 * text content and associated metadata.</p>
 *
 * @param text the document text content
 * @param metadata key-value pairs of document metadata
 */
public record KbDocument(
    String text,
    Map<String, Object> metadata) {
}

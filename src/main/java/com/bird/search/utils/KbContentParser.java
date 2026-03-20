//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.bird.search.utils;

import com.bird.search.dto.KbDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

/**
 * Utility class for parsing Bedrock Knowledge Base document content.
 *
 * <p>Handles deserialization of raw KB document text into {@link KbDocument} objects,
 * with graceful fallback to plain text if parsing fails.</p>
 */
public class KbContentParser {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  /**
   * Parses raw KB document text into a structured KbDocument.
   *
   * <p>Attempts to deserialize as JSON; if parsing fails, returns the raw text
   * as-is with empty metadata.</p>
   *
   * @param rawText raw document text from Bedrock KB
   * @return parsed KbDocument, or plain-text fallback on failure
   */
  public static KbDocument parse(String rawText) {
    try {
      return MAPPER.readValue(rawText, KbDocument.class);
    } catch (Exception var2) {
      return new KbDocument(rawText, Map.of());
    }
  }
}

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.bird.search.utils;

import java.util.Map;
import java.util.stream.Collectors;
import software.amazon.awssdk.core.document.Document;

/**
 * Utility class for converting AWS SDK Document objects to plain Java Maps.
 *
 * <p>Provides conversion from Bedrock SDK's {@link Document} type to standard
 * Java collections for easier serialization and handling.</p>
 */
public class BedrockDocumentMapper {

  /**
   * Converts a map of Bedrock Document objects to a plain Java Map.
   *
   * @param metadata AWS SDK Document map to convert
   * @return plain Java map with unwrapped values, or empty map if input is null
   */
  public static Map<String, Object> toPlainMap(Map<String, Document> metadata) {
    return metadata == null ? Map.of() : (Map) metadata.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, (e) -> unwrap((Document) e.getValue())));
  }

  /**
   * Recursively unwraps a Bedrock Document to native Java types.
   *
   * <p>Handles Document types: string, number, boolean, list, map, and null.</p>
   *
   * @param doc Bedrock Document to unwrap
   * @return unwrapped Java object (String, Number, Boolean, List, Map, or null)
   */
  private static Object unwrap(Document doc) {
    if (doc == null) {
      return null;
    } else if (doc.isString()) {
      return doc.asString();
    } else if (doc.isNumber()) {
      return doc.asNumber();
    } else if (doc.isBoolean()) {
      return doc.asBoolean();
    } else if (doc.isList()) {
      return doc.asList().stream().map(BedrockDocumentMapper::unwrap).toList();
    } else {
      return doc.isMap() ? doc.asMap().entrySet().stream()
          .collect(Collectors.toMap(Map.Entry::getKey, (e) -> unwrap((Document) e.getValue())))
          : null;
    }
  }
}

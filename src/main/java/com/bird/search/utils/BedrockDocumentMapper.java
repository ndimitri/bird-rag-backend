//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.bird.search.utils;

import java.util.Map;
import java.util.stream.Collectors;
import software.amazon.awssdk.core.document.Document;

public class BedrockDocumentMapper {
  public static Map<String, Object> toPlainMap(Map<String, Document> metadata) {
    return metadata == null ? Map.of() : (Map)metadata.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (e) -> unwrap((Document)e.getValue())));
  }

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
      return doc.isMap() ? doc.asMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (e) -> unwrap((Document)e.getValue()))) : null;
    }
  }
}

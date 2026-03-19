//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.bird.search.utils;

import com.bird.search.dto.KbDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class KbContentParser {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static KbDocument parse(String rawText) {
    try {
      return (KbDocument) MAPPER.readValue(rawText, KbDocument.class);
    } catch (Exception var2) {
      return new KbDocument(rawText, Map.of());
    }
  }
}

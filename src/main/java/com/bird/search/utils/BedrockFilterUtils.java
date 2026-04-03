package com.bird.search.utils;

import java.util.Map;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import software.amazon.awssdk.core.document.Document;
import software.amazon.awssdk.services.bedrockagentruntime.model.FilterAttribute;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrievalFilter;

/**
 * Utility helpers for building Bedrock retrieval filters from API-level metadata filters.
 */
@UtilityClass
public class BedrockFilterUtils {

  /**
   * Extracts the value of a line starting with {@code CODE:}.
   *
   * @param text raw knowledge base content
   * @return extracted code or {@code null} when not found
   */
  public static String extractCode(String text) {
    for (String line : text.split("\n")) {
      if (line.startsWith("CODE:")) {
        return line.substring("CODE:".length()).trim();
      }
    }
    return null;
  }

  /**
   * Builds a Bedrock {@link RetrievalFilter} from a key and raw value.
   *
   * <p>Supported operators: {@code >=}, {@code <=}, {@code >}, {@code <}. If no operator is
   * provided, an equals filter is produced.</p>
   *
   * @param key metadata field name
   * @param rawValue metadata filter value
   * @return filter instance or {@code null} when input is blank/invalid
   */
  public static RetrievalFilter buildFilter(String key, String rawValue) {
    if (rawValue == null || rawValue.isBlank()) {
      return null;
    }

    String trimmed = rawValue.trim();

    try {
      if (trimmed.startsWith(">=")) {
        long n = Long.parseLong(trimmed.substring(2).trim());
        return RetrievalFilter.builder()
            .greaterThanOrEquals(FilterAttribute.builder().key(key).value(Document.fromNumber(n)).build())
            .build();
      }

      if (trimmed.startsWith("<=")) {
        long n = Long.parseLong(trimmed.substring(2).trim());
        return RetrievalFilter.builder()
            .lessThanOrEquals(FilterAttribute.builder().key(key).value(Document.fromNumber(n)).build())
            .build();
      }

      if (trimmed.startsWith(">")) {
        long n = Long.parseLong(trimmed.substring(1).trim());
        return RetrievalFilter.builder()
            .greaterThan(FilterAttribute.builder().key(key).value(Document.fromNumber(n)).build())
            .build();
      }

      if (trimmed.startsWith("<")) {
        long n = Long.parseLong(trimmed.substring(1).trim());
        return RetrievalFilter.builder()
            .lessThan(FilterAttribute.builder().key(key).value(Document.fromNumber(n)).build())
            .build();
      }
    } catch (NumberFormatException e) {
      return null;
    }

    return RetrievalFilter.builder()
        .equalsValue(FilterAttribute.builder().key(key).value(Document.fromString(trimmed)).build())
        .build();
  }

  /**
   * Builds a single Bedrock filter from a map of metadata constraints.
   *
   * @param filters map of key/value filters
   * @return combined filter ({@code andAll}), a single filter, or {@code null}
   */
  public static RetrievalFilter buildAndFilter(Map<String, String> filters) {
    if (filters == null || filters.isEmpty()) {
      return null;
    }

    var filterList = filters.entrySet().stream()
        .map(e -> buildFilter(e.getKey(), e.getValue()))
        .filter(Objects::nonNull)
        .toList();

    if (filterList.isEmpty()) {
      return null;
    }

    if (filterList.size() == 1) {
      return filterList.getFirst();
    }

    return RetrievalFilter.builder().andAll(filterList).build();
  }
}

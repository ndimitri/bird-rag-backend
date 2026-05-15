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
      return switch (getOperator(trimmed)) {
        case ">=" -> {
          long n = Long.parseLong(trimmed.substring(2).trim());
          yield RetrievalFilter.builder()
              .greaterThanOrEquals(FilterAttribute.builder().key(key).value(Document.fromNumber(n)).build())
              .build();
        }
        case "<=" -> {
          long n = Long.parseLong(trimmed.substring(2).trim());
          yield RetrievalFilter.builder()
              .lessThanOrEquals(FilterAttribute.builder().key(key).value(Document.fromNumber(n)).build())
              .build();
        }
        case ">" -> {
          long n = Long.parseLong(trimmed.substring(1).trim());
          yield RetrievalFilter.builder()
              .greaterThan(FilterAttribute.builder().key(key).value(Document.fromNumber(n)).build())
              .build();
        }
        case "<" -> {
          long n = Long.parseLong(trimmed.substring(1).trim());
          yield RetrievalFilter.builder()
              .lessThan(FilterAttribute.builder().key(key).value(Document.fromNumber(n)).build())
              .build();
        }
        default -> RetrievalFilter.builder()
            .equalsValue(FilterAttribute.builder().key(key).value(Document.fromString(trimmed)).build())
            .build();
      };
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * Determines the comparison operator present at the beginning of a raw value.
   *
   * <p>Recognized operators are {@code >=}, {@code <=}, {@code >}, and {@code <}. If no operator is detected, an empty string is returned.</p>
   *
   * @param trimmed already-trimmed value to analyze *
   * @return the detected operator, or an empty string if no operator is present
   */
   private static String getOperator(String trimmed) {
   if (trimmed.startsWith(">=") || trimmed.startsWith("<=")) {
   return trimmed.substring(0,2);
   }
   if (trimmed.startsWith(">") || trimmed.startsWith("<")) {
   return trimmed.substring(0,1);
   }
   return "";
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

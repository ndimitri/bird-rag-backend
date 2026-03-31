package com.bird.search.dto;

import java.util.Map;
import lombok.Data;

@Data
public class MetadataSearchRequest {
  private String query;
  private Map<String, String> filters;

}

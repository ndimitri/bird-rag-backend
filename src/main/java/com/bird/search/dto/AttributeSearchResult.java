package com.bird.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttributeSearchResult {

  private String code;
  private String name;
  private String description;
  private String domainId;
  private String maintenanceAgencyId;
  private double score;

}

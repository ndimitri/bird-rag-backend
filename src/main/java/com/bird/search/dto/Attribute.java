package com.bird.search.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // optionnel mais pratique si ton JSON évolue
public class Attribute {

  @JsonProperty("MAINTENANCE_AGENCY_ID")
  private String maintenanceAgencyId;

  @JsonProperty("VARIABLE_ID")
  private String variableId;

  @JsonProperty("CODE")
  private String code;

  @JsonProperty("NAME")
  private String name;

  @JsonProperty("DESCRIPTION")
  private String description;

  @JsonProperty("DOMAIN_ID")
  private String domainId;

  @JsonProperty("VALID_FROM")
  private String validFrom;

  @JsonProperty("VALID_TO")
  private String validTo;

}
package com.bird.search.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Metadata {
  private String entity_type;
  private String maintenance_agency;
  private String id;
  private String code;
  private String title;
  private String domain_id;
  private String parent_variable_id;
  private String valid_from;
  private String valid_to;
  private String description;

}

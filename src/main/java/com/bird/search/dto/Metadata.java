package com.bird.search.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for attribute metadata.
 *
 * <p>Represents the complete structure of a metadata attribute in the data governance system,
 * including entity classification, identifiers, temporal validity, and description.</p>
 */
@Getter
@Setter
public class Metadata {

  /** Type of entity (e.g., "variable"). */
  private String entity_type;

  /** Agency responsible for maintaining this metadata. */
  private String maintenance_agency;

  /** Unique identifier for this attribute. */
  private String id;

  /** Code identifier used in systems. */
  private String code;

  /** Human-readable title of the attribute. */
  private String title;

  /** Domain identifier for classification. */
  private String domain_id;

  /** Parent variable identifier, if hierarchically nested. */
  private String parent_variable_id;

  /** Start date of validity in YYYY-MM-DD format. */
  private String valid_from;

  /** End date of validity in YYYY-MM-DD format. */
  private String valid_to;

  /** Detailed description of the attribute. */
  private String description;

}

package com.bird.search.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for attribute generation requests.
 *
 * <p>Contains the partial description provided by the user that will be used
 * as input to the AI model for generating a complete attribute.</p>
 */
@Getter
@Setter
public class AttributeGenerationRequest {

  /** Partial description of the attribute to generate. */
  private String partialDescription;

}

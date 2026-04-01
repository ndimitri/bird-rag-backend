package com.bird.search.dto;

import java.time.LocalDate;
import java.time.ZoneOffset;
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
  private String validFrom;
  private String validTo;
  private Long validFromUnix;
  private Long validToUnix;
  private double score;

  public static AttributeSearchResult fromAttribute(Attribute attribute, double score) {
    return new AttributeSearchResult(
        attribute.getCode(),
        attribute.getName(),
        attribute.getDescription(),
        attribute.getDomainId(),
        attribute.getMaintenanceAgencyId(),
        attribute.getValidFrom(),
        attribute.getValidTo(),
        toUnixTimestamp(attribute.getValidFrom()),
        toUnixTimestamp(attribute.getValidTo()),
        score
    );
  }


  private static Long toUnixTimestamp(String date) {
    if (date == null || date.isBlank()) {
      return null;
    }
    return LocalDate.parse(date)
        .atStartOfDay(ZoneOffset.UTC)
        .toEpochSecond();
  }


}

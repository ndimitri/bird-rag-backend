package com.bird.search.dto;



public record LegalDocumentSearchResult(
    //Bedrock metadata fields
    String title,
    String text,
    String s3Uri,
    Double score,


    //Custom metadata fields
    String entityType,
    String documentType,
    String regulation,
    String article,
    String attachedVariable,
    String jurisdiction,
    String validFrom,
    String sourceUrl

) {

}

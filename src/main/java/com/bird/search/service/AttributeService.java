package com.bird.search.service;

import com.bird.search.dto.Attribute;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class AttributeService {

  private final List<Attribute> attributes;

  public AttributeService() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    attributes = mapper.readValue(
        new ClassPathResource("VARIABLE.001.json").getInputStream(),
        new TypeReference<>() {
        }
    );
  }

  public Optional<Attribute> getByCode(String code){
    return attributes.stream()
        .filter(attribute -> code.equals(attribute.getCode()))
        .findFirst();

  }


}

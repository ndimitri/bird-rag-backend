//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.bird.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot application entry point for Bird RAG (Retrieval-Augmented Generation).
 *
 * <p>Main class that initializes the application, enabling automatic component
 * scanning and configuration for the Bird Search REST API powered by AWS Bedrock.</p>
 */
@SpringBootApplication
public class BirdSearchApplication {

  /**
   * Main method to start the Spring Boot application.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(BirdSearchApplication.class, args);
  }
}

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.bird.search.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring web configuration for CORS (Cross-Origin Resource Sharing).
 *
 * <p>Enables CORS for API endpoints to support frontend requests from
 * development and production origins.</p>
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

  /**
   * Configures CORS mappings for API endpoints.
   *
   * <p>Allows GET, POST, and OPTIONS methods from configured origins
   * with all headers permitted.</p>
   *
   * @param registry CORS registry to configure
   */
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
        .allowedOrigins("http://localhost:5173", "http://localhost:3000", "*")
        .allowedMethods("GET", "POST", "OPTIONS")
        .allowedHeaders("*");
  }
}

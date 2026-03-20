//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.bird.search.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

/**
 * Spring configuration for AWS Bedrock clients.
 *
 * <p>Provides singleton beans for Bedrock Runtime and Agent Runtime clients
 * configured with region and credentials from application properties.</p>
 */
@Configuration
public class AwsConfig {

  @Value("${aws.region}")
  private String region;

  @Value("${aws.access-key-id}")
  private String accessKeyId;

  @Value("${aws.secret-access-key}")
  private String secretAccessKey;

  /**
   * Creates a BedrockRuntimeClient bean.
   *
   * <p>Configured with AWS region and static credentials from properties.</p>
   *
   * @return configured BedrockRuntimeClient instance
   */
  @Bean
  public BedrockRuntimeClient bedrockRuntimeClient() {
    return BedrockRuntimeClient.builder()
        .region(Region.of(this.region))
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(this.accessKeyId, this.secretAccessKey)))
        .build();
  }

  /**
   * Creates a BedrockAgentRuntimeClient bean.
   *
   * <p>Configured with AWS region and static credentials from properties.</p>
   *
   * @return configured BedrockAgentRuntimeClient instance
   */
  @Bean
  public BedrockAgentRuntimeClient bedrockAgentRuntimeClient() {
    return BedrockAgentRuntimeClient.builder()
        .region(Region.of(this.region))
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(this.accessKeyId, this.secretAccessKey)))
        .build();
  }

}

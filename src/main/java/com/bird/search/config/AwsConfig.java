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
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClientBuilder;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClientBuilder;

@Configuration
public class AwsConfig {
  @Value("${aws.region}")
  private String region;
  @Value("${aws.access-key-id}")
  private String accessKeyId;
  @Value("${aws.secret-access-key}")
  private String secretAccessKey;

  @Bean
  public BedrockRuntimeClient bedrockRuntimeClient() {
    return (BedrockRuntimeClient)((BedrockRuntimeClientBuilder)((BedrockRuntimeClientBuilder)BedrockRuntimeClient.builder().region(Region.of(this.region))).credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(this.accessKeyId, this.secretAccessKey)))).build();
  }

  @Bean
  public BedrockAgentRuntimeClient bedrockAgentRuntimeClient() {
    return (BedrockAgentRuntimeClient)((BedrockAgentRuntimeClientBuilder)((BedrockAgentRuntimeClientBuilder)BedrockAgentRuntimeClient.builder().region(Region.of(this.region))).credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(this.accessKeyId, this.secretAccessKey)))).build();
  }


}

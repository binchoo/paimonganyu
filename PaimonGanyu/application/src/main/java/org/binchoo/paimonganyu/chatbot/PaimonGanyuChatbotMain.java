package org.binchoo.paimonganyu.chatbot;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories({"org.binchoo.paimonganyu.chatbot.view"}) // todo: remove this line
@SpringBootApplication(scanBasePackages = {
        "org.binchoo.paimonganyu.chatbot",
        "org.binchoo.paimonganyu.infra.hoyopass",
        "org.binchoo.paimonganyu.service.hoyopass",
})
public class PaimonGanyuChatbotMain {

    public static void main(String[] args) {
        SpringApplication paimonGanyu = new SpringApplication(PaimonGanyuChatbotMain.class);
        paimonGanyu.setAdditionalProfiles("test");
        paimonGanyu.run();
    }

    @Profile("test")
    @Configuration
    @PropertySource("classpath:amazon.properties")
    public class TestAmazonClientsConfig {

        @Value("${amazon.dynamodb.endpoint}")
        private String dynamoEndpoint;

        @Value("${amazon.region}")
        private String region;

        @Value("${amazon.aws.accesskey}")
        private String accessKey;

        @Value("${amazon.aws.secretkey}")
        private String secretKey;

        @Bean
        public AWSCredentialsProvider awsCredentialsProvider() {
            return new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
        }

        @Primary
        @Bean(name = {"testAmazonDynamoDB", "amazonDynamoDB"})
        public AmazonDynamoDB testAmazonDynamoDB(AWSCredentialsProvider credentialsProvider) {
            AmazonDynamoDBClientBuilder dynamoDBClientBuilder
                    = AmazonDynamoDBClientBuilder.standard();
            dynamoDBClientBuilder.setEndpointConfiguration(dynamoEndpointConfig());
            dynamoDBClientBuilder.setCredentials(credentialsProvider);
            System.out.println("Using test AmazonDynamoDB");
            return dynamoDBClientBuilder.build();
        }

        private AwsClientBuilder.EndpointConfiguration dynamoEndpointConfig() {
            return new AwsClientBuilder.EndpointConfiguration(dynamoEndpoint, region);
        }

        @Primary
        @Bean
        public AWSSimpleSystemsManagement testSsmClient(AWSCredentialsProvider credentialsProvider) {
            AWSSimpleSystemsManagementClientBuilder ssmBuilder = AWSSimpleSystemsManagementClientBuilder.standard();
            ssmBuilder.setRegion(region);
            ssmBuilder.setCredentials(credentialsProvider);
            System.out.println("Using test ssmClient");
            return ssmBuilder.build();
        }
    }
}

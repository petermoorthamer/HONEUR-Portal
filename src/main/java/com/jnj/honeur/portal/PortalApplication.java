package com.jnj.honeur.portal;

import com.amazonaws.auth.BasicAWSCredentials;
import com.jnj.honeur.aws.s3.AmazonS3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Configuration
@ControllerAdvice
@SpringBootApplication
public class PortalApplication extends SpringBootServletInitializer {

	private static Logger log = LoggerFactory.getLogger(PortalApplication.class);

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(PortalApplication.class);
	}

	@Bean(name = "honeurCentralAmazonS3Service")
	public AmazonS3Service honeurCentralAmazonS3Service(@Value("${honeur.central.amazon.accessKey}") String accessKey, @Value("${honeur.central.amazon.secretKey}") String secretKey) {
		return new AmazonS3Service(new BasicAWSCredentials(accessKey, secretKey));
	}

    @Bean(name = "honeurLocalAmazonS3Service")
    public AmazonS3Service honeurLocalAmazonS3Service(@Value("${honeur.local.amazon.accessKey}") String accessKey, @Value("${honeur.local.amazon.secretKey}") String secretKey) {
        return new AmazonS3Service(new BasicAWSCredentials(accessKey, secretKey));
    }

    @Bean(name = "moorthamerAmazonS3Service")
    public AmazonS3Service moorthamerAmazonS3Service(@Value("${moorthamer.amazon.accessKey}") String accessKey, @Value("${moorthamer.amazon.secretKey}") String secretKey) {
        return new AmazonS3Service(new BasicAWSCredentials(accessKey, secretKey));
    }



	public static void main(String[] args) {
		SpringApplication.run(PortalApplication.class, args);
	}
}

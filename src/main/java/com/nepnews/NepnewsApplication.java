package com.nepnews;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableMethodSecurity
@SpringBootApplication
@EnableMongoAuditing  // Enable auto timestamping for MongoDB
public class NepnewsApplication {
	public static void main(String[] args) {
		SpringApplication.run(NepnewsApplication.class, args);
	}
}

package com.nepnews;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing  // Enable auto timestamping for MongoDB
public class NepnewsApplication {
	public static void main(String[] args) {
		SpringApplication.run(NepnewsApplication.class, args);
	}
}

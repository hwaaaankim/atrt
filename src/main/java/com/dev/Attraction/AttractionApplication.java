package com.dev.Attraction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AttractionApplication {

	public static void main(String[] args) {
		SpringApplication.run(AttractionApplication.class, args);
	}

}

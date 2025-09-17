package com.unilink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling

public class UnilinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnilinkApplication.class, args);
	}

}

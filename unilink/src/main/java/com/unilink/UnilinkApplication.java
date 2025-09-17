package com.unilink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class UnilinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnilinkApplication.class, args);
	}

}

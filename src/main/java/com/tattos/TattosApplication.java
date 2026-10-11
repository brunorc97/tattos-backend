package com.tattos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TattosApplication {

	public static void main(String[] args) {
		SpringApplication.run(TattosApplication.class, args);
	}

}

package com.example.abonnements_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Enable scheduled tasks
public class AbonnementsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AbonnementsServiceApplication.class, args);
		System.out.println("Abonnements Service started successfully!");
	}

}

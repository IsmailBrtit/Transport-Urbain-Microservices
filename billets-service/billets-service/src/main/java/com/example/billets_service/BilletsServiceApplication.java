package com.example.billets_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BilletsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BilletsServiceApplication.class, args);
		System.out.println("Billets Service started successfully!");
	}

}

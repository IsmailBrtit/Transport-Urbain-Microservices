package com.example.abonnements_service;

import com.example.abonnements_service.model.Abonnement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class AbonnementsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AbonnementsServiceApplication.class, args);
		System.out.println("it works" );
	}

}

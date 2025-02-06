package com.licencjat.BusinessAssistant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class BusinessAssistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(BusinessAssistantApplication.class, args);
	}


}

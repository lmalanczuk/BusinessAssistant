package com.licencjat.BusinessAssistant;

import com.licencjat.BusinessAssistant.client.AIMicroserviceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class BusinessAssistantApplication implements CommandLineRunner {

	@Autowired
	private AIMicroserviceClient aiMicroserviceClient;
	public static void main(String[] args) {
		SpringApplication.run(BusinessAssistantApplication.class, args);
	}
 	 @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
	 @Override
    public void run(String... args) throws Exception {
        aiMicroserviceClient.callTranscribeEndpoint();
    }
}

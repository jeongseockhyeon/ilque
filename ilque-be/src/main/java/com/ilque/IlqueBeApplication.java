package com.ilque;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class IlqueBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(IlqueBeApplication.class, args);
	}

}

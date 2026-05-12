package com.cuestacougars.cuesta_cougars_web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.cuestacougars")
public class CuestaCougarsWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(CuestaCougarsWebApplication.class, args);
	}

}

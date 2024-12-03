package com.project.plateposting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class PlatepostingApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlatepostingApplication.class, args);
	}

}

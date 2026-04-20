package br.com.escola.academic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AcademicCoreServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AcademicCoreServiceApplication.class, args);
	}

}

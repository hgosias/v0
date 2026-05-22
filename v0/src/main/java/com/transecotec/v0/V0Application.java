package com.transecotec.v0;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.transecotec")
public class V0Application {

	public static void main(String[] args) {
		SpringApplication.run(V0Application.class, args);
	}

}

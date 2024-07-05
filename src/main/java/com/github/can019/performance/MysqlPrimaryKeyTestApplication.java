package com.github.can019.performance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.github.can019.performance")
public class MysqlPrimaryKeyTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(MysqlPrimaryKeyTestApplication.class, args);
	}

}

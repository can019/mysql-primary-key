package com.github.can019.performance;

import org.springframework.boot.SpringApplication;

public class TestMysqlPrimaryKeyTestApplication {

	public static void main(String[] args) {
		SpringApplication.from(MysqlPrimaryKeyTestApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

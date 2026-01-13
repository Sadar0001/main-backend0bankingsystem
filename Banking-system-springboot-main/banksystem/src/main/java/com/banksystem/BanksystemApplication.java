package com.banksystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
//@EnableRetry
@EnableTransactionManagement
public class BanksystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(BanksystemApplication.class, args);
	}

}

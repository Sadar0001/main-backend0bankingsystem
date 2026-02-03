package com.banksystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
// @EnableRetry // Agar zarurat nahi hai to commented hi rehne de
// @CrossOrigin hata diya gaya hai taaki SecurityConfig control kare
@EnableTransactionManagement
public class BanksystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(BanksystemApplication.class, args);
    }

}
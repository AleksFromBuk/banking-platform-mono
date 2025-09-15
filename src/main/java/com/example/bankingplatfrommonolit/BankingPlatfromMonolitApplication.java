package com.example.bankingplatfrommonolit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BankingPlatfromMonolitApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingPlatfromMonolitApplication.class, args);
    }

}

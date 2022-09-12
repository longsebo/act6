package com.activiti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(exclude={
        org.activiti.spring.boot.SecurityAutoConfiguration.class,
        SecurityAutoConfiguration.class
})
@EnableTransactionManagement
@ComponentScan({"org.activiti.rest","com.activiti"})
public class ActApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActApplication.class, args);
    }

}

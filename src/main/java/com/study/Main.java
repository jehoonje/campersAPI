package com.study;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        // Spring Boot 애플리케이션을 실행하는 코드
        SpringApplication.run(Main.class, args);

        // 콘솔 메시지 출력
        System.out.printf("Hello and welcome to the Spring Boot application!");

        for (int i = 1; i <= 5; i++) {
            System.out.println("i = " + i);
        }
    }
}

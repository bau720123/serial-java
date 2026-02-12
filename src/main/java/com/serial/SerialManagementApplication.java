package com.serial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 序號管理系統啟動入口
 * Spring Boot 4.0.2 / Java 25 / Jakarta EE 11 / No Lombok
 */
@SpringBootApplication
public class SerialManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(SerialManagementApplication.class, args);
    }
}

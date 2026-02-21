package com.serial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 序號管理系統啟動入口
 *
 * <p>這是整個 Spring Boot 應用程式的進入點。</p>
 *
 * <ul>
 *   <li>框架版本：Spring Boot 4.0.2</li>
 *   <li>Java 版本：Java 25（支援 Virtual Threads）</li>
 *   <li>規格：Jakarta EE 11</li>
 *   <li>無 Lombok 依賴（所有 getter/setter 手動撰寫）</li>
 * </ul>
 *
 * <p>@SpringBootApplication 是組合註解，包含：</p>
 * <ul>
 *   <li>@SpringBootConfiguration – 標記為 Spring 設定類別</li>
 *   <li>@EnableAutoConfiguration – 啟用自動設定</li>
 *   <li>@ComponentScan – 自動掃描同 package 下的 Bean</li>
 * </ul>
 */
@SpringBootApplication
public class SerialManagementApplication {

    /**
     * 應用程式進入點。
     *
     * @param args 命令列參數（通常為空）
     */
    public static void main(String[] args) {
        SpringApplication.run(SerialManagementApplication.class, args);
    }
}

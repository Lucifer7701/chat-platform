package com.dating;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 在线交友平台主启动类
 *
 * @author Dating Platform Team
 * @version 1.0
 */
@SpringBootApplication
@MapperScan("com.dating.mapper")
@EnableTransactionManagement
@EnableScheduling
public class DatingPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatingPlatformApplication.class, args);
        System.out.println("=================================");
        System.out.println("在线交友平台启动成功！");
        System.out.println("API文档地址: http://localhost:8080/swagger-ui.html");
        System.out.println("=================================");
    }
}
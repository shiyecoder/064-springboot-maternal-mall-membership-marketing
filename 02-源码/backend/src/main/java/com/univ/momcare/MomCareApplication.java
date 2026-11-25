package com.univ.momcare;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 芽芽母婴商城与会员营销系统 启动类。
 * 端口 8082、库 new_064 由 application.yml 指定，通道 3 专用。
 */
@SpringBootApplication
@MapperScan("com.univ.momcare.mapper")
@EnableScheduling
public class MomCareApplication {

    public static void main(String[] args) {
        SpringApplication.run(MomCareApplication.class, args);
    }
}

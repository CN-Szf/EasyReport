package com.sdyx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 启动程序
 *
 * @author EasyReport
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan({"com.sdyx", "org.anyline"})
@EntityScan("com.sdyx")
@EnableJpaRepositories("com.sdyx.report.mapper")
@EnableJpaAuditing
public class EasyReportApplication {
    public static void main(String[] args) {
        SpringApplication.run(EasyReportApplication.class, args);
        System.out.println("EasyReport启动成功 \n");
    }
}

package org.sommiersys.sommiersys;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;



@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
@EntityScan(basePackages = "org.pack.sommierJar.*")
public class SommierSysApplication {

    public static void main(String[] args) {
        SpringApplication.run(SommierSysApplication.class, args);
    }
}

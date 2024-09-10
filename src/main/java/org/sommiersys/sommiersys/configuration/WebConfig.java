package org.sommiersys.sommiersys.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig{

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET" , "POST" , "PUT" , "DELETE" )
                        .maxAge(3600);
            }
        };
    }/*
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/404.html").setViewName("404");
    }*/
}
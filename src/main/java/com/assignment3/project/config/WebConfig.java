package com.assignment3.project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:images/");
        registry.addResourceHandler("/docs/**")
                .addResourceLocations("file:docs/");
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:avatars/");
    }
}
package com.codeyzer.codeyzersi.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${spring.mvc.cors.allowed-origins}")
    private String allowedOrigins;
    
    @Value("${spring.mvc.cors.allowed-methods:POST, GET, OPTIONS, DELETE, PUT}")
    private String allowedMethods;
    
    @Value("${spring.mvc.cors.allowed-headers:Authorization, Content-Type, Accept, X-Requested-With}")
    private String allowedHeaders;
    
    @Value("${spring.mvc.cors.exposed-headers:Content-Disposition, Content-Length}")
    private String exposedHeaders;
    
    @Value("${spring.mvc.cors.max-age:3600}")
    private String maxAge;
    
    @Value("${spring.mvc.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        String[] methods = allowedMethods.split(",");
        String[] headers = allowedHeaders.split(",");
        String[] exposed = exposedHeaders.split(",");
        
        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods(methods)
                .allowedHeaders(headers)
                .exposedHeaders(exposed)
                .allowCredentials(allowCredentials)
                .maxAge(Long.parseLong(maxAge));
    }
    
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        String[] origins = allowedOrigins.split(",");
        for (String origin : origins) {
            corsConfiguration.addAllowedOrigin(origin.trim());
        }
        
        Arrays.stream(allowedMethods.split(","))
                .map(String::trim)
                .forEach(corsConfiguration::addAllowedMethod);
        
        if ("*".equals(allowedHeaders)) {
            corsConfiguration.addAllowedHeader("*");
        } else {
            Arrays.stream(allowedHeaders.split(","))
                    .map(String::trim)
                    .forEach(corsConfiguration::addAllowedHeader);
        }
        
        Arrays.stream(exposedHeaders.split(","))
                .map(String::trim)
                .forEach(corsConfiguration::addExposedHeader);
        
        corsConfiguration.setAllowCredentials(allowCredentials);
        corsConfiguration.setMaxAge(Long.parseLong(maxAge));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(source);
    }
} 
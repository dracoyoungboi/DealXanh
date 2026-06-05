package com.dealxanh.app.config;

import com.dealxanh.app.security.BuyerAccessInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private BuyerAccessInterceptor buyerAccessInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(buyerAccessInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/admin/**", "/moderator/**", "/seller/**", "/staff/**",
                        "/api/**", "/css/**", "/js/**", "/img/**", "/uploads/**",
                        "/login", "/admin/login", "/seller/login", "/logout",
                        "/buyer/deal-map",
                        "/favicon.ico", "/error");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get("uploads");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}

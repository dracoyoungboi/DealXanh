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

    @Autowired
    private com.dealxanh.app.security.MaintenanceInterceptor maintenanceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Maintenance check runs first — redirects non-staff to /maintenance
        // Admin/Seller/Staff/Login paths excluded: they bypass via role check anyway,
        // so skip the interceptor entirely for speed.
        registry.addInterceptor(maintenanceInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/admin/**", "/moderator/**", "/seller/**", "/staff/**",
                        "/login", "/admin/login", "/seller/login", "/perform_login",
                        "/logout", "/oauth2/**",
                        "/css/**", "/js/**", "/img/**", "/images/**",
                        "/uploads/**", "/static/**", "/favicon.ico",
                        "/error", "/maintenance");

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

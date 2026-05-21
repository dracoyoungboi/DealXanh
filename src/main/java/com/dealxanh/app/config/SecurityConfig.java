package com.dealxanh.app.config;

import com.dealxanh.app.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private com.dealxanh.app.security.CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Autowired
    private com.dealxanh.app.security.CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Autowired
    private com.dealxanh.app.security.CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Static resources - luôn public
                .requestMatchers(
                    "/css/**", "/js/**", "/img/**", "/images/**",
                    "/uploads/**", "/static/**"
                ).permitAll()

                // Auth routes - public
                .requestMatchers(
                    "/login", "/admin/login", "/seller/login",
                    "/register", "/register/buyer", "/seller/register",
                    "/forgot-password", "/admin/forgot-password", "/seller/forgot-password",
                    "/reset-password", "/terms", "/logout",
                    "/oauth2/**", "/login/oauth2/**", "/api/auth/**", "/perform_login"
                ).permitAll()

                // Onboarding pending page - allow authenticated users (role may be pending update)
                .requestMatchers("/seller/onboarding-pending", "/auth/onboarding-pending").authenticated()

                // Admin routes - chỉ ADMIN và MODERATOR
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "MODERATOR")

                // Seller routes - chỉ STORE_OWNER và STORE_STAFF
                .requestMatchers("/seller/**", "/store/dashboard/**").hasAnyRole("STORE_OWNER")

                // Staff routes - chỉ STORE_STAFF (dùng chung cổng login /seller/login)
                .requestMatchers("/staff/**").hasRole("STORE_STAFF")

                // Buyer routes - public (không cần đăng nhập)
                .requestMatchers("/", "/home", "/shop/**", "/product/**", "/store/**").permitAll()

                // Buyer routes - cần đăng nhập
                .requestMatchers("/profile", "/checkout/**", "/my-orders/**",
                    "/buyer/profile", "/buyer/checkout/**",
                    "/buyer/payment", "/buyer/order-complete",
                    "/buyer/checkout/confirm").authenticated()

                // Cart - public
                .requestMatchers("/cart/**", "/buyer/cart", "/category/**", "/deals/**",
                    "/buyer/search", "/buyer/store/**", "/buyer/deal-map",
                    "/buyer/orders", "/buyer/orders/**").permitAll()

                // Mặc định - cho phép tất cả
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/perform_login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(customAuthenticationSuccessHandler)
                .failureHandler(customAuthenticationFailureHandler)
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
                .invalidSessionStrategy(new com.dealxanh.app.security.CustomInvalidSessionStrategy())
                .maximumSessions(5)
                .maxSessionsPreventsLogin(false)
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("dealxanh-remember-me-secret-key-2024")
                .tokenValiditySeconds(7 * 24 * 60 * 60) // 7 ngày
                .userDetailsService(customUserDetailsService)
                .rememberMeParameter("rememberme")
                .rememberMeCookieName("remember-me")
            )
            .userDetailsService(customUserDetailsService)
            // Tắt CSRF cho các API endpoint AJAX, login và admin forms
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/cart/add", "/cart/update", "/cart/remove",
                    "/cart/clear", "/cart/apply-coupon", "/cart/remove-coupon",
                    "/api/**", "/perform_login",
                    "/admin/seller-verify/*/approve", "/admin/seller-verify/*/reject",
                    "/admin/seller-verify/*/reset-to-pending",
                    "/admin/api/finance/**", "/admin/api/dispute/**",
                    "/admin/deals/*/update", "/admin/deals/create",
                    "/admin/deals/*/pause", "/admin/deals/*/resume",
                    "/admin/deals/*/assign-categories", "/admin/deals/*/remove-category/*",
                    "/admin/deals/*/assign-products", "/admin/deals/*/remove-product/*",
                    "/seller/deals/*/assign-products", "/seller/deals/*/remove-product/*",
                    "/seller/deals/*/pause", "/seller/deals/*/resume",
                    "/seller/products/*/toggle", "/seller/products/*/edit",
                    "/seller/api/products/*/delete", "/seller/api/products/quick-push",
                    "/seller/orders/*/status",
                    "/seller/api/finance/**",
                    "/seller/profile/update-store", "/seller/profile/update-documents", "/seller/profile/change-password",
                    "/seller/store-page/update",
                    "/seller/manage-staff/add", "/seller/manage-staff/*/remove", "/seller/manage-staff/*/toggle",
                    "/staff/orders/*/status",
                    "/staff/profile/update", "/staff/profile/change-password",
                    "/staff/products/create",
                    "/pickup/*/confirm",
                    "/buyer/checkout/confirm",
                    "/moderator/seller-verify/*/approve", "/moderator/seller-verify/*/reject"
                )
            )
                .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new com.dealxanh.app.security.CustomAuthenticationEntryPoint())
                .accessDeniedPage("/login?error=access_denied")
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(customAuthenticationSuccessHandler)
            );

        return http.build();
    }
}

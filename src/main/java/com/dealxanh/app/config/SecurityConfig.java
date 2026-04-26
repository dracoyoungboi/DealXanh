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
                    "/login", "/admin/login", "/seller/login", "/register", "/register/buyer",
                    "/seller/register", "/forgot-password", "/admin/forgot-password",
                    "/seller/forgot-password", "/reset-password", "/terms",
                    "/logout", "/oauth2/**", "/login/oauth2/**", "/api/auth/**"
                ).permitAll()

                // Admin routes - ADMIN và MODERATOR
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "MODERATOR")

                // Seller routes - STORE_OWNER và STORE_STAFF
                .requestMatchers("/seller/**", "/store/dashboard/**").hasAnyRole("STORE_OWNER", "STORE_STAFF")

                // Checkout, giỏ hàng, đơn hàng - cần đăng nhập
                .requestMatchers("/checkout/**", "/my-orders/**").authenticated()

                // Cart - public (có thể xem giỏ hàng không cần đăng nhập)
                .requestMatchers("/cart/**").permitAll()

                // Trang chủ và shop - public
                .requestMatchers("/", "/home", "/shop/**", "/product/**", "/store/**").permitAll()

                // Profile - cần đăng nhập
                .requestMatchers("/profile/**").authenticated()

                // Mặc định - cho phép tất cả
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/perform_login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(customAuthenticationSuccessHandler)
                .failureUrl("/login?error=true")
                .permitAll()
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
            // Tắt CSRF cho các API endpoint AJAX
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/cart/add", "/cart/update", "/cart/remove",
                    "/cart/clear", "/cart/apply-coupon", "/cart/remove-coupon",
                    "/api/**"
                )
            )
                .exceptionHandling(exceptions -> exceptions
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

package com.example.healthflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/auth/register", "/auth/login", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/book/**", "/loginbook").permitAll()
                        .requestMatchers("/patient/**").hasRole("PATIENT")
                        .requestMatchers("/doctor/**").hasRole("DOCTOR")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/patient/**").hasRole("PATIENT")
                        .requestMatchers("/api/doctor/**").hasRole("DOCTOR")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .successHandler(roleBasedAuthenticationSuccessHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/auth/login?logout")
                        .permitAll()
                )
                // Completely disable CSRF
                .csrf().disable()
                .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler roleBasedAuthenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {
                Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

                if (roles.contains("ROLE_ADMIN")) {
                    response.sendRedirect("/admin/dashboard");
                } else if (roles.contains("ROLE_DOCTOR")) {
                    response.sendRedirect("/doctor/dashboard");
                } else if (roles.contains("ROLE_PATIENT")) {
                    response.sendRedirect("/patient/dashboard");
                } else {
                    response.sendRedirect("/");
                }
            }
        };
    }
}
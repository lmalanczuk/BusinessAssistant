package com.licencjat.BusinessAssistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
//        http
//                .authorizeHttpRequests((requests) -> requests
//                .requestMatchers("/").permitAll()
//                .anyRequest().authenticated()
//                )
//                .formLogin((form) -> form
//                        .loginPage("/login")
//                        .permitAll()
//                )
//                .logout((logout) -> logout.permitAll())
//                .exceptionHandling((exceptionHandling) -> exceptionHandling
//                        .accessDeniedPage("/access-denied"));
//        return http.build();
//    }
//
//    @Bean
//    public UserDetailsService userDetailsService() {
//      UserDetails user =
//        User.withDefaultPasswordEncoder()
//          .username("user")
//          .password("password")
//          .roles("ADMIN")
//          .build();
//        return new InMemoryUserDetailsManager(user);
//    }
     @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/zoom/oauth/callback", "/api/zoom/webhook").permitAll()
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable())
            .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }
}

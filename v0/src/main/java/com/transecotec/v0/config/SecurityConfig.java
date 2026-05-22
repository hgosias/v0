package com.transecotec.v0.config; // Ajusta este paquete al tuyo

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // 1. Instanciamos el motor de cifrado
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Le decimos a Spring que no bloquee nuestro Frontend
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desactiva la protección CSRF (necesario para APIs REST)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Permite todas las peticiones sin pedir token por ahora
                );
        return http.build();
    }
}
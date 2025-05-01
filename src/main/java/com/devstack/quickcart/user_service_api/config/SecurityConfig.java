package com.devstack.quickcart.user_service_api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    JwtAuthConverter jwtAuthConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
       /* http.cors(httpSecurityCorsConfigurer ->
                httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()));*/
        http.authorizeHttpRequests(authorize -> {
            authorize
                    .requestMatchers(HttpMethod.POST, "user-service/api/v1/users/signup").permitAll()
                    .requestMatchers(HttpMethod.POST, "user-service/api/v1/users/login").permitAll()
                    .requestMatchers(HttpMethod.POST, "user-service/api/v1/users/forgot-password-request-code").permitAll()
                    .requestMatchers(HttpMethod.POST, "user-service/api/v1/users/verify-reset").permitAll()
                    .requestMatchers(HttpMethod.POST, "user-service/api/v1/users/reset-password").permitAll()
                    .requestMatchers(HttpMethod.POST, "user-service/api/v1/users/resend").permitAll()
                    .requestMatchers(HttpMethod.POST, "user-service/api/v1/users/verify-email").permitAll()
                    .requestMatchers(HttpMethod.GET, "user-service/api/v1/users/visitor/**").permitAll()

                    .anyRequest().authenticated();
        });

        http.oauth2ResourceServer(t -> {
            t.jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwtAuthConverter));
            /*  t.jwt((Customizer.withDefaults()));*/
        });
        http.sessionManagement(t -> t.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public DefaultMethodSecurityExpressionHandler msecurity() {
        DefaultMethodSecurityExpressionHandler defaultMethodSecurityExpressionHandler = new DefaultMethodSecurityExpressionHandler();
        defaultMethodSecurityExpressionHandler.setDefaultRolePrefix("");
        return defaultMethodSecurityExpressionHandler;
    }

   /* @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
    }*/

}
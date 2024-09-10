package com.example.transacciones.config;

import com.example.transacciones.config.filter.JwtTokenValidator;
import com.example.transacciones.services.UserDetailServiceIMP;

import com.example.transacciones.util.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity

public class SecurityConfig {

    @Autowired
    JWTUtils jwtUtils;

    AuthenticationConfiguration authenticationConfiguration;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {


        return httpSecurity.csrf(csrf->csrf.disable()) // Desactiva CSRF (Cross-Site Request Forgery)
                .httpBasic(Customizer.withDefaults()) //Autorizacion a traves de usuarios
                .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //Manejo de sesion sin estado
                .addFilterBefore(new JwtTokenValidator(jwtUtils), BasicAuthenticationFilter.class)//Validaciones para asignar token
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean //Proveedor de autentificacion
    public AuthenticationProvider authenticationProvider(UserDetailServiceIMP userDetailService) throws Exception{
        DaoAuthenticationProvider provider=new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailService);

        return provider;
    }

    @Bean //Algoritmo de encriptacion de Contraseñas
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


}

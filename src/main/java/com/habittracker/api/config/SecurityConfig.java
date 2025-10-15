package com.habittracker.api.config;

import com.habittracker.api.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService(){
        return new UserDetailsServiceImpl();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity
            .authorizeHttpRequests(auth -> auth
                // Allow public access to these pages and static resources
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**").permitAll()
                // All other requests must be authenticated
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login") // Specify our custom login page
                .defaultSuccessUrl("/dashboard", true) // Redirect to dashboard on success
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout") // Redirect to login on logout
                .permitAll()
            );

        return http.build();
    }
}
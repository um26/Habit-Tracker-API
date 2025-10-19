package com.habittracker.api.config;

import com.habittracker.api.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired // Inject the custom success handler needed for 2FA redirect
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Bean // Provide our UserDetailsService implementation
    public UserDetailsService userDetailsService(){
        return new UserDetailsServiceImpl();
    }

    @Bean // Provide the password encoder
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean // Configure the Authentication Provider to use our UserDetailsService and PasswordEncoder
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService()); // Use our email-based loader
        authenticationProvider.setPasswordEncoder(passwordEncoder()); // Use BCrypt for password checks
        return authenticationProvider;
    }

    @Bean // Configure the main security rules
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for simpler form handling
            .authorizeHttpRequests(auth -> auth
                // Define public URLs that don't require login
                .requestMatchers("/", "/login", "/register",
                                 "/verify-email", "/registration-success",
                                 "/css/**", "/js/**").permitAll()
                // All other URLs require the user to be authenticated
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login") // Specify our custom login page URL
                 // IMPORTANT: Tell Spring Security the HTML input field named 'username' contains the email
                .usernameParameter("username")
                // Use our custom handler to decide where to go after password check (2FA or Dashboard)
                .successHandler(customAuthenticationSuccessHandler)
                .permitAll() // Allow everyone to access the login page itself
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout") // Where to go after logging out
                .permitAll() // Allow everyone to log out
            );

        return http.build();
    }
}
package com.habittracker.api.config;

import com.habittracker.api.model.User;
import com.habittracker.api.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String email = authentication.getName();
        log.info("Password authentication successful for email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database: " + email));

        HttpSession session = request.getSession();

        if (user.isTwoFactorEnabled()) { // Uses isTwoFactorEnabled()
            log.info("2FA is enabled for user {}. Redirecting to 2FA verification.", email);
            session.setAttribute("TFA_USER_EMAIL", email);
            response.sendRedirect("/verify-2fa");
        } else {
            log.info("2FA is NOT enabled for user {}. Redirecting to dashboard.", email);
            response.sendRedirect("/dashboard");
        }
    }
}
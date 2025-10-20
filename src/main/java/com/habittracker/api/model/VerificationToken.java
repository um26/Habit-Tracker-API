package com.habittracker.api.model;

import jakarta.persistence.*;
import lombok.Getter; // <-- Ensure imported
import lombok.NoArgsConstructor;
import lombok.Setter; // <-- Ensure imported

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

@Getter // <-- Add annotation
@Setter // <-- Add annotation
@NoArgsConstructor
@Entity
public class VerificationToken {

    private static final int EXPIRATION = 60 * 24; // 24 hours

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    private Date expiryDate;

    public VerificationToken(String token, User user) {
        this.token = token;
        this.user = user;
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }

    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }
}
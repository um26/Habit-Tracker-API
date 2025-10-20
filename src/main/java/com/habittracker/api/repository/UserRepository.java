package com.habittracker.api.repository;

import com.habittracker.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email); // Primary lookup

    Optional<User> findByUsername(String username); // For checking uniqueness & security

    Optional<User> findByUniqueUserId(String uniqueUserId);

    List<User> findByUsernameOrUniqueUserId(String username, String uniqueUserId); // For friend search
}
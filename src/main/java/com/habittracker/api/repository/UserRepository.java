package com.habittracker.api.repository;

import com.habittracker.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUniqueUserId(String uniqueUserId);
List<User> findByUsernameOrUniqueUserId(String username, String uniqueUserId);
}
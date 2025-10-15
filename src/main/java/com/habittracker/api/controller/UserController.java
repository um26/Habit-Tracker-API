package com.habittracker.api.controller;

import com.habittracker.api.model.User;
import com.habittracker.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


// This controller is not needed for web registration, as WebController handles registration.
// If you want to support API registration, you can re-add this controller with a different path.
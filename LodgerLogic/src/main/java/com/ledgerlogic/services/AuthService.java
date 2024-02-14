package com.ledgerlogic.services;

import com.ledgerlogic.models.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserService userService;

    public AuthService(UserService userService) {
        this.userService = userService;
    }

    public Optional<User> findByCredentials(String username, String password) {
        return userService.findByCredentials(username, password);
    }

    public User register(User user) {
        return userService.upsert(user);
    }
}
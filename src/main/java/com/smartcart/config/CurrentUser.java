package com.smartcart.config;

import com.smartcart.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    public User get(HttpServletRequest request) {
        Object user = request.getAttribute("currentUser");
        if (user instanceof User) return (User) user;
        throw new IllegalStateException("Current user not found in request");
    }
}

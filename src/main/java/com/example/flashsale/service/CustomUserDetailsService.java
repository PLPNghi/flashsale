package com.example.flashsale.service;

import com.example.flashsale.entity.User;

public interface CustomUserDetailsService {
    User getUserByUsername(String username);
}

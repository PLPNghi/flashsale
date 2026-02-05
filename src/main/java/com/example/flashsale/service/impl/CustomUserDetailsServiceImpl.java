package com.example.flashsale.service.impl;

import com.example.flashsale.entity.User;
import com.example.flashsale.repository.UserRepository;
import com.example.flashsale.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService, UserDetailsService {
    private final UserRepository userRepository;

    /**
     * Loads user details by username (email or phone number).
     * @param username the username (email or phone number) to look up
     * @return UserDetails object containing user authentication information
     * @throws UsernameNotFoundException if user cannot be found by email or phone
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .or(() -> userRepository.findByPhone(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new org.springframework.security.core.userdetails.User(username, user.getPasswordHash(), new ArrayList<>());
    }

    /**
     * Retrieves the actual User entity by username (email or phone).
     * @param username the username (email or phone number) to look up
     * @return the complete User entity from database
     * @throws UsernameNotFoundException if user cannot be found by email or phone
     */
    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByEmail(username)
                .or(() -> userRepository.findByPhone(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}

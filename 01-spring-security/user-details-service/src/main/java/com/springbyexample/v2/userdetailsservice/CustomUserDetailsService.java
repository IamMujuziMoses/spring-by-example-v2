package com.springbyexample.v2.userdetailsservice;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author Mujuzi Moses
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {

        if (!"user".equals(username)) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return User.withUsername("user").password("{noop}password").roles("USER").build();
    }
}

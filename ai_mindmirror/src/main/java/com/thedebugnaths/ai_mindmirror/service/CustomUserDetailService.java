package com.thedebugnaths.ai_mindmirror.service;

import com.thedebugnaths.ai_mindmirror.auth.SecurityUserDto;
import com.thedebugnaths.ai_mindmirror.exception.ResourceNotFoundException;
import com.thedebugnaths.ai_mindmirror.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return new SecurityUserDto(userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found.")));
    }
}

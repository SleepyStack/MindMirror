package com.thedebugnaths.ai_mindmirror.service;

import com.thedebugnaths.ai_mindmirror.dto.AuthRequest;
import com.thedebugnaths.ai_mindmirror.dto.AuthResponse;
import com.thedebugnaths.ai_mindmirror.auth.JwtUtil;
import com.thedebugnaths.ai_mindmirror.dto.RegisterUserRequest;
import com.thedebugnaths.ai_mindmirror.entity.User;
import com.thedebugnaths.ai_mindmirror.exception.DuplicateKeyException;
import com.thedebugnaths.ai_mindmirror.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse registerUser(RegisterUserRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new DuplicateKeyException("Email is already in use!");
        }
        User savedUser = new User(request.username(), request.email(), passwordEncoder.encode(request.password()));
        userRepository.save(savedUser);

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());

        String jwtToken = jwtUtil.generateToken(userDetails);
        return new AuthResponse(jwtToken);
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());

        String jwtToken = jwtUtil.generateToken(userDetails);

        return new AuthResponse(jwtToken);
    }
}
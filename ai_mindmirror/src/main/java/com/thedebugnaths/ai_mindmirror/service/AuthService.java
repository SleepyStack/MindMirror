package com.thedebugnaths.ai_mindmirror.service;

import com.thedebugnaths.ai_mindmirror.dto.AuthRequest;
import com.thedebugnaths.ai_mindmirror.dto.AuthResponse;
import com.thedebugnaths.ai_mindmirror.auth.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

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
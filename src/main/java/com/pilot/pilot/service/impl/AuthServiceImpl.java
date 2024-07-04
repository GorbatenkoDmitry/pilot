package com.pilot.pilot.service.impl;

import com.pilot.pilot.service.AuthService;
import com.pilot.pilot.web.dto.auth.JwtRequest;
import com.pilot.pilot.web.dto.auth.JwtResponse;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    @Override
    public JwtResponse login(JwtRequest loginRequest) {
        return null;
    }

    @Override
    public JwtResponse refresh(String refreshToken) {
        return null;
    }
}

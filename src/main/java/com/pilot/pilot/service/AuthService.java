package com.pilot.pilot.service;

import com.pilot.pilot.web.dto.auth.JwtRequest;
import com.pilot.pilot.web.dto.auth.JwtResponse;
import org.springframework.stereotype.Service;

@Service

public interface AuthService {

    JwtResponse login(JwtRequest loginRequest);

    JwtResponse refresh(String refreshToken);

}

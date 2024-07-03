package com.pilot.pilot.service;

import org.springframework.stereotype.Service;

@Service

public interface AuthService {

    JwtResponse login(JwtRequest loginRequest);

    JwtResponce refresh(String refreshToken);

}

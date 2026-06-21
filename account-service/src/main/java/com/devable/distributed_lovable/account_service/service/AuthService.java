package com.devable.distributed_lovable.account_service.service;


import com.devable.distributed_lovable.account_service.dto.auth.AuthResponse;
import com.devable.distributed_lovable.account_service.dto.auth.LoginRequest;
import com.devable.distributed_lovable.account_service.dto.auth.SignupRequest;

public interface AuthService {
    AuthResponse signup(SignupRequest request);

    AuthResponse login(LoginRequest request);
}

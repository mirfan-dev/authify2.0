package com.security.service;

public interface AuthService {

    public void sendResetOtp(String email);

    public void resetPassword(String email, String otp, String newPassword);

    public void sendOtp(String email);

    public void verifyOtp(String email, String otp);
}

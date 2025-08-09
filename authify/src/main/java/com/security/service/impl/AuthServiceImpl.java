package com.security.service.impl;

import com.security.entity.User;
import com.security.repository.UserRepository;
import com.security.service.AuthService;
import com.security.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void sendResetOtp(String email) {
        User user=userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("User not found with this email "+email));

        // generate otp
        String otp= OtpUtil.generateOtp();

        // otp expiry time
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(15);;

        // update user entity
        user.setResetOtp(otp);
        user.setResetOtpExpiredAt(expiryTime);

        userRepository.save(user);

        try{
            emailService.sendResetOtp(user.getEmail(),otp);
        }catch (Exception ex){
            throw new RuntimeException("Unable to send otp");
        }


    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {

        User user=userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("User not found with this email "+email));

        if (user.getResetOtp()==null || !user.getResetOtp().equals(otp)){
            throw new RuntimeException("Invalid Otp");
        }
        if (user.getResetOtpExpiredAt().isBefore(LocalDateTime.now())){
            throw new RuntimeException("OTP expired");
        }


        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetOtp(null);
        user.setResetOtpExpiredAt(null);

        userRepository.save(user);

    }

    @Override
    public void sendOtp(String email) {

        User user=userRepository.findByEmail(email)
                .orElseThrow(()->new RuntimeException("User not found with this email "+email));

        if (user.getIsAccountVerifiedAt() !=null && user.getIsAccountVerifiedAt()){
            return;
        }
        // Generate 6 digit otp
        String otp=OtpUtil.generateOtp();

        // otp expiry time 24 hours
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(24);


        // update user entity
        user.setVerifyOtp(otp); // Fixed
        user.setVerifyOtpExpiredAt(expiryTime);

        userRepository.save(user);

        try{
            emailService.sendOtp(user.getEmail(),otp);
        }catch (Exception ex){
            throw new RuntimeException("Unable to send otp");
        }
    }

    @Override
    public void verifyOtp(String email, String otp) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email " + email));

        if (user.getVerifyOtp() == null || ! user.getVerifyOtp().equals(otp)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }

        if (user.getVerifyOtpExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        user.setIsAccountVerifiedAt(true);
        user.setVerifyOtp(null);
        user.setVerifyOtpExpiredAt(null);


        userRepository.save(user);
    }
}

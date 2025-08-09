package com.security.controller;

import com.security.config.JwtToken;
import com.security.dto.AuthRequest;
import com.security.dto.AuthResponse;
import com.security.dto.RefreshTokenRequest;
import com.security.dto.ResetPasswordRequest;
import com.security.service.AuthService;
import com.security.service.UserService;
import com.security.service.impl.AppUserDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AppUserDetailsService appUserDetailService;
    private final AuthenticationManager authentication;
    private final JwtToken jwtToken;



    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {

        try {
            authenticate(request.getEmail(), request.getPassword());

            final UserDetails userDetails = appUserDetailService.loadUserByUsername(request.getEmail());

            // Generate Access and Refresh Tokens
            final String accessToken = jwtToken.generateToken(userDetails, true);  // access token
            final String refreshToken = jwtToken.generateToken(userDetails, false); // refresh token

            // Optional: Set refresh token in HTTP-only cookie
            ResponseCookie accessCookie = ResponseCookie.from("jwt", accessToken)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(Duration.ofMinutes(15)) // or Duration.ofDays(1)
                    .sameSite("Strict")
                    .build();

            ResponseCookie refreshCookie = ResponseCookie.from("refresh", refreshToken)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(Duration.ofDays(7))
                    .sameSite("Strict")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(new AuthResponse(request.getEmail(), accessToken, refreshToken));

        } catch (BadCredentialsException ex) {
            return errorResponse("Email or Password is incorrect", HttpStatus.BAD_REQUEST);
        } catch (DisabledException ex) {
            return errorResponse("User account is disabled", HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            return errorResponse("Authentication failed", HttpStatus.UNAUTHORIZED);
        }
    }

    // Helper method for error response
    private ResponseEntity<?> errorResponse(String message, HttpStatus status) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", message);
        return ResponseEntity.status(status).body(error);
    }

    // Authentication logic
    private void authenticate(String email, String password) {
        authentication.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body("Refresh token is missing.");
        }

        try {
            String email = jwtToken.extractEmail(refreshToken);
            UserDetails userDetails = appUserDetailService.loadUserByUsername(email);

            if (!jwtToken.isRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not a valid refresh token.");
            }

            if (!jwtToken.validateToken(refreshToken, userDetails)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token.");
            }

            String newAccessToken = jwtToken.generateAccessToken(userDetails);

            ResponseCookie cookie = ResponseCookie.from("accessJwt", newAccessToken)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(Duration.ofMinutes(15))
                    .sameSite("Strict")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new AuthResponse(email, newAccessToken,null));

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token.");
        }
    }

    @GetMapping("/is-authenticated")
    public ResponseEntity<Boolean> isAuthenticated(@CurrentSecurityContext(expression = "authentication?.name") String email){
        return ResponseEntity.ok(email != null);
    }

    @PostMapping("/send-reset-otp")
    public void sendResetOtp(@RequestParam String email){
        try{
            authService.sendResetOtp(email);
        }catch (Exception ex){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,ex.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request){

        try {

            authService.resetPassword(request.getEmail(),request.getOtp(),request.getNewPassword());

        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage());
        }

    }

    @PostMapping("/send-otp")
    public void sendOtp(@CurrentSecurityContext(expression = "authentication?.name") String email){
        try {
            authService.sendOtp(email);
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage());
        }

    }

    @PostMapping("/verify-otp")
    public void verifyOtp(
            @RequestBody Map<String,Object> request,
            @CurrentSecurityContext(expression = "authentication?.name") String email

    ){
        if (request.get("otp").toString().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Missing Data");
        }

        String otp=request.get("otp").toString();
        authService.verifyOtp(email,otp);



    }




}

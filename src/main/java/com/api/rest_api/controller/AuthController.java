package com.api.rest_api.controller;

import com.api.rest_api.dto.*;
import com.api.rest_api.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // Đăng ký tài khoản
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> registerAccount(@RequestBody RegisterRequest request) {
        return authService.registerAccount(request);
    }

    // resend otp
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody ResendOtpRequest request) {
        return authService.resendOtp(request);
    }

    // Xác minh OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        return authService.verifyOtp(request);
    }

    // Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // Quên mật khẩu - gửi OTP qua email
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestBody LoginRequest request) {
        return authService.updatePassword(request);
    }

    // Đặt lại mật khẩu
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }

    @GetMapping("/hello")
    public ResponseEntity<?> hello() {
        return ResponseEntity.ok("Quizflow said hello");
    }

    // New endpoint to get user by UID
    @GetMapping("/users/{uid}")
    public ResponseEntity<?> getUserByUid(@PathVariable Long uid) {
        return authService.getUserByUid(uid);
    }

    // Updated endpoint to update profile using UID
    @PostMapping(value = "/update-profile", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateProfile(
            @RequestPart("uid") String uid,
            @RequestPart("username") String username,
            @RequestPart("fullname") String fullname,
            @RequestPart("email") String email,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            Long uidLong = Long.parseLong(uid);
            return authService.updateProfile(uidLong, username, fullname, email, image);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(new APIResponse("Invalid UID format"));
        }
    }

    @PutMapping("/{uid}/coins")
    public ResponseEntity<Void> updateUserCoins(@PathVariable Long uid, @RequestBody CoinUpdateRequest request) {
        authService.updateUserCoins(uid, request.getCoins());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rankings")
    public ResponseEntity<List<Map<String, Object>>> getRankings(@RequestParam(required = false, defaultValue = "all") String period) {
        try {
            System.out.println("Rankings requested with period: " + period);
            List<Map<String, Object>> rankings = authService.getRankings(period);
            System.out.println("Returning " + rankings.size() + " rankings");
            return ResponseEntity.ok(rankings);
        } catch (Exception e) {
            System.err.println("Error in getRankings: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}

package com.api.rest_api.service;

import com.api.rest_api.dto.*;
import com.api.rest_api.model.Account;
import com.api.rest_api.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    private Map<String, RegisterRequest> tempAccounts = new HashMap<>();

    public ResponseEntity<?> register(RegisterRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã tồn tại!");
        }

        tempAccounts.put(request.getEmail(), request);

        String otp = otpService.generateOtp(request.getEmail());
        emailService.sendEmail(request.getEmail(), "Xác minh tài khoản", "OTP của bạn: " + otp);

        return ResponseEntity.ok("Đăng ký thành công, vui lòng kiểm tra email để xác minh!");
    }

    public ResponseEntity<?> resendOtp(ResendOtpRequest request) {
        String otp = otpService.generateOtp(request.getEmail());
        emailService.sendEmail(request.getEmail(), "Xác minh tài khoản", "OTP của bạn: " + otp);

        return ResponseEntity.ok("Đã gữi mã OTP!");
    }

    public ResponseEntity<?> verifyOtp(VerifyOtpRequest request) {
        if (!otpService.verifyOtp(request.getEmail(), request.getOtp())) {
            return ResponseEntity.badRequest().body("OTP không hợp lệ!");
        }

        RegisterRequest registerRequest = tempAccounts.get(request.getEmail());
        if (registerRequest == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy thông tin đăng ký!");
        }

        return ResponseEntity.ok("Xác minh tài khoản thành công!");
    }

    public ResponseEntity<?> registerAccount(RegisterRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã tồn tại!");
        }

        Account account = new Account();
        account.setEmail(request.getEmail());
        account.setFullname(request.getFullname());
        account.setUsername(request.getUsername());
        account.setPassword(request.getPassword());

        accountRepository.save(account);
        tempAccounts.remove(request.getEmail());

        return ResponseEntity.ok("Tạo tài khoản thành công!");
    }

    public ResponseEntity<?> login(LoginRequest request) {
        Account account = accountRepository.findByEmailAndPassword(request.getEmail(), request.getPassword());
        if (account == null) {
            return ResponseEntity.badRequest().body("Email hoặc mật khẩu không đúng!");
        }
        return ResponseEntity.ok("Đăng nhập thành công!");
    }

    public ResponseEntity<?> forgotPassword(ForgotPasswordRequest request) {
        if (!accountRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email không tồn tại!");
        }

        String otp = otpService.generateOtp(request.getEmail());
        emailService.sendEmail(request.getEmail(), "Quên mật khẩu", "OTP của bạn: " + otp);

        return ResponseEntity.ok("OTP đã được gửi qua email.");
    }

    public ResponseEntity<?> resetPassword(ResetPasswordRequest request) {
        if (!otpService.verifyOtp(request.getEmail(), request.getOtp())) {
            return ResponseEntity.badRequest().body("OTP không hợp lệ!");
        }

        Account account = accountRepository.findByEmail(request.getEmail());
        account.setPassword(request.getNewPassword());
        accountRepository.save(account);

        return ResponseEntity.ok("Đặt lại mật khẩu thành công!");
    }
}

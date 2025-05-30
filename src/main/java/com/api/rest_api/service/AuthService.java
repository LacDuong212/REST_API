package com.api.rest_api.service;

import com.api.rest_api.dto.*;
import com.api.rest_api.model.Account;
import com.api.rest_api.model.CoinHistory;
import com.api.rest_api.repository.AccountRepository;
import com.api.rest_api.repository.CoinHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.stream.Collectors;

@Service
public class AuthService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CoinHistoryRepository coinHistoryRepository;

    private Map<String, RegisterRequest> tempAccounts = new HashMap<>();

    private static final String UPLOAD_DIR = "D:/Uploads/";

    public ResponseEntity<?> register(RegisterRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã tồn tại!");
        }

        if (accountRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username đã tồn tại!");
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

//        RegisterRequest registerRequest = tempAccounts.get(request.getEmail());
//        if (registerRequest == null) {
//            return ResponseEntity.badRequest().body("Không tìm thấy thông tin đăng ký!");
//        }

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
        account.setPassword(passwordEncoder.encode(request.getPassword()));

        accountRepository.save(account);
        tempAccounts.remove(request.getEmail());

        return ResponseEntity.ok("Tạo tài khoản thành công!");
    }

    public ResponseEntity<?> login(LoginRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail());
        if (account == null || !passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            return ResponseEntity.badRequest().body("Email hoặc mật khẩu không đúng!");
        }
        // Tạo đối tượng DTO để chứa thông tin người dùng
        UserResponse userResponse = new UserResponse();
        userResponse.setUid(account.getUid());
        userResponse.setUsername(account.getUsername());
        userResponse.setFullname(account.getFullname());
        userResponse.setEmail(account.getEmail());
        userResponse.setImage(account.getImage());
        userResponse.setCoins(account.getCoins());

        // Trả về thông tin người dùng cùng thông báo đăng nhập thành công
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đăng nhập thành công!");
        response.put("user", userResponse);

        return ResponseEntity.ok(response);
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
        Account account = accountRepository.findByUid(request.getUid());
        if (account == null) {
            return ResponseEntity.badRequest().body("Tài khoản không tồn tại!");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), account.getPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu chưa chính xác!");
        }

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
        return ResponseEntity.ok("Đặt lại mật khẩu thành công!");
    }

    // New method to get user by UID
    public ResponseEntity<UserResponse> getUserByUid(Long uid) {
        return accountRepository.findById(uid)
                .map(account -> {
                    UserResponse userResponse = new UserResponse();
                    userResponse.setUid(account.getUid());
                    userResponse.setUsername(account.getUsername());
                    userResponse.setFullname(account.getFullname());
                    userResponse.setEmail(account.getEmail());
                    userResponse.setImage(account.getImage());
                    userResponse.setCoins(coinHistoryRepository.sumAmountByAccountUid(uid));
                    return ResponseEntity.ok(userResponse);
                })
                .orElseGet(() -> ResponseEntity.badRequest().body(null));
    }

    // Phương thức này không cần thực hiện bất kỳ thao tác nào vì client sẽ gọi createCoinHistory trực tiếp
    public void updateUserCoins(Long uid, Integer coins) {
        // Chỉ xác minh user tồn tại
        Account account = accountRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Không thực hiện thêm bất kỳ thao tác nào
        // Số dư coins được tính bằng cách cộng tổng các bản ghi CoinHistory
    }

    // Updated method to update profile using UID
    public ResponseEntity<?> updateProfile(Long uid, String username, String fullname, String email, MultipartFile image) {
        return accountRepository.findById(uid)
                .map(account -> {
                    // Validate inputs
                    if (username == null || username.trim().isEmpty()) {
                        return ResponseEntity.badRequest().body(new APIResponse("Username không được để trống!"));
                    }
                    if (fullname == null || fullname.trim().isEmpty()) {
                        return ResponseEntity.badRequest().body(new APIResponse("Họ tên không được để trống!"));
                    }
                    if (email == null || email.trim().isEmpty()) {
                        return ResponseEntity.badRequest().body(new APIResponse("Email không được để trống!"));
                    }

                    // Check for duplicate username or email
                    if (!account.getUsername().equals(username) && accountRepository.existsByUsername(username)) {
                        return ResponseEntity.badRequest().body(new APIResponse("Username đã tồn tại!"));
                    }
                    if (!account.getEmail().equals(email) && accountRepository.existsByEmail(email)) {
                        return ResponseEntity.badRequest().body(new APIResponse("Email đã tồn tại!"));
                    }

                    // Update account details
                    account.setUsername(username);
                    account.setFullname(fullname);
                    account.setEmail(email);

                    // Handle image upload
                    if (image != null && !image.isEmpty()) {
                        try {
                            // Generate unique filename
                            String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                            Path filePath = Paths.get(UPLOAD_DIR + fileName);
                            Files.createDirectories(filePath.getParent());
                            image.transferTo(filePath.toFile());

                            // Store relative URL
                            String imageUrl = "/Uploads/" + fileName;
                            account.setImage(imageUrl);
                        } catch (IOException e) {
                            return ResponseEntity.badRequest().body(new APIResponse("Lỗi khi tải lên hình ảnh!"));
                        }
                    }

                    accountRepository.save(account);
                    // Return updated user data
                    UserResponse userResponse = new UserResponse();
                    userResponse.setUid(account.getUid());
                    userResponse.setUsername(account.getUsername());
                    userResponse.setFullname(account.getFullname());
                    userResponse.setEmail(account.getEmail());
                    userResponse.setImage(account.getImage());
                    userResponse.setCoins(account.getCoins());

                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Cập nhật hồ sơ thành công!");
                    response.put("user", userResponse);

                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.badRequest().body(new APIResponse("Tài khoản không tồn tại!")));
    }

    public ResponseEntity<?> updatePassword(LoginRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail());

        if (account == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy tài khoản!");
        }

        account.setPassword(passwordEncoder.encode(request.getPassword()));
        accountRepository.save(account);

        return ResponseEntity.ok("Cập nhật mật khẩu thành công!");

    }

    public List<Map<String, Object>> getRankings(String period) {
        LocalDate today = LocalDate.now();
        Date startDate = null;

        // Calculate start date based on period
        if ("weekly".equals(period)) {
            // Start of current week (Monday)
            LocalDate monday = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            startDate = Date.from(monday.atStartOfDay(ZoneId.systemDefault()).toInstant());
            System.out.println("Weekly start date: " + startDate);
        } else if ("monthly".equals(period)) {
            // Start of current month
            LocalDate firstDayOfMonth = today.withDayOfMonth(1);
            startDate = Date.from(firstDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
            System.out.println("Monthly start date: " + startDate);
        }
        // For "all", startDate remains null

        List<Object[]> results;
        List<Map<String, Object>> rankings = new ArrayList<>();
        
        try {
            if (startDate != null) {
                results = coinHistoryRepository.findUserRankingsByPeriod(startDate);
            } else {
                results = coinHistoryRepository.findAllTimeUserRankings();
            }
            
            for (Object[] result : results) {
                if (result.length >= 5) {
                    Map<String, Object> ranking = new HashMap<>();
                    ranking.put("id", result[0]);
                    ranking.put("username", result[1]);
                    ranking.put("fullname", result[2]);
                    ranking.put("image", result[3]);
                    ranking.put("coins", result[4]);
                    rankings.add(ranking);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching rankings: " + e.getMessage());
            e.printStackTrace();
        }
        
        return rankings.subList(0, Math.min(rankings.size(), 20)); // Limit to top 20
    }

    public ResponseEntity<?>searchAccountsByUsername(String keyword) {
        String search = keyword == null ? "" : keyword.trim();

        List<Account> accounts = accountRepository.searchAccountsByUsername(search);

        if (accounts.isEmpty()) {
            return ResponseEntity.ok("Không tìm thấy người dùng nào!");
        }

        List<UserModel> userModels = accounts.stream()
                .map(UserModel::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(userModels);
    }
}

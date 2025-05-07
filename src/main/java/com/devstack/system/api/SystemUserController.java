package com.devstack.system.api;

import com.devstack.system.dto.request.RequestUserDto;
import com.devstack.system.dto.request.RequestUserLoginRequest;
import com.devstack.system.dto.request.RequestUserPasswordResetDto;
import com.devstack.system.dto.response.ResponseUserDetailsDto;
import com.devstack.system.service.UserService;
import com.devstack.system.service.impl.JwtService;
import com.devstack.system.util.StandardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/user-service/api/v1/users")
@RequiredArgsConstructor
public class SystemUserController {

    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/signup")
    public ResponseEntity<StandardResponse> createUser(@RequestBody RequestUserDto dto) throws IOException {
        userService.createUser(dto);
        return new ResponseEntity<>(
                new StandardResponse(201,
                        "Account was created. verify your email now (%s)".formatted(dto.getEmail()), null),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    public ResponseEntity<StandardResponse> loginUser(@RequestBody RequestUserLoginRequest dto) {
        return new ResponseEntity<>(
                new StandardResponse(200,
                        "Login Successful!", userService.userLogin(dto)),
                HttpStatus.OK
        );
    }

    @PostMapping("/verify-email")
    public ResponseEntity<StandardResponse> verifyEmail(@RequestParam String otp, @RequestParam String email) {
        boolean isVerified = userService.verifyEmail(otp, email);
        if (isVerified) {
            return new ResponseEntity<>(
                    new StandardResponse(200,
                            "Account was Verified. Please log in", null),
                    HttpStatus.OK
            );
        } else {
            return new ResponseEntity<>(
                    new StandardResponse(400,
                            "Invalid OTP. Please insert the correct code to verify your email.", null),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PostMapping(path = {"/resend"}, params = {"email"})
    public ResponseEntity<StandardResponse> resend(
            @RequestParam String email) throws IOException {
        userService.resend(email);
        return new ResponseEntity<>(
                new StandardResponse(201,
                        "OTP resent to your registered email".formatted(email), null),
                HttpStatus.CREATED
        );
    }

    @PostMapping(path = {"/forgot-password-request-code"}, params = {"email"})
    public ResponseEntity<StandardResponse> forgotPasswordSendVerificationCode(
            @RequestParam String email) throws IOException {

        userService.forgotPasswordSendVerificationCode(email);
        return new ResponseEntity<>(
                new StandardResponse(201,
                        "Password reset verification code has been sent", null),
                HttpStatus.CREATED
        );
    }

    @PostMapping(path = {"/verify-reset"}, params = {"otp", "email"})
    public ResponseEntity<StandardResponse> verifyReset(
            @RequestParam String otp, @RequestParam String email) {


        boolean isVerified = userService.verifyReset(otp, email);
        if (isVerified) {
            return new ResponseEntity<>(
                    new StandardResponse(200,
                            "Please reset your password now", true),
                    HttpStatus.OK
            );
        } else {
            return new ResponseEntity<>(
                    new StandardResponse(400,
                            "Invalid OTP. Please insert the correct code to verify your email.", false),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PostMapping(path = {"/reset-password"})
    public ResponseEntity<StandardResponse> passwordReset(
            @RequestBody RequestUserPasswordResetDto dto) {
        return new ResponseEntity<>(
                new StandardResponse(200,
                        "Password reset completed successfully", userService.passwordReset(dto)),
                HttpStatus.OK
        );


    }

    @GetMapping("/verify-admin")
    public ResponseEntity<StandardResponse> verifyAdmin(
            @RequestHeader("Authorization") String tokenHeader
    ) {
        String token = tokenHeader.replace("Bearer ", "");
        String email = jwtService.getEmail(token);


        boolean isVerified = userService.verifyAdmin(email);
        if (isVerified) {
            return new ResponseEntity<>(
                    new StandardResponse(200,
                            "Admin was Verified!", true),
                    HttpStatus.OK
            );
        } else {
            return new ResponseEntity<>(
                    new StandardResponse(400,
                            "User is not an Admin!", false),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("/visitor/verify-role")
    public ResponseEntity<StandardResponse> verifyRole(
            @RequestHeader("Authorization") String tokenHeader,
            @RequestParam String role
    ) {
        String token = tokenHeader.replace("Bearer ", "");
        String email = jwtService.getEmail(token);

        boolean isVerified = userService.verifyRole(email, role);
        return new ResponseEntity<>(
                new StandardResponse(200,
                        isVerified?"User was Verified!":"User is not Authorized!", isVerified),
                HttpStatus.OK
        );
    }

    @GetMapping("/user/check-role")
    public ResponseEntity<StandardResponse> verifyStudent(
            @RequestHeader("Authorization") String tokenHeader,
            @RequestParam String email
    ) {

        boolean isVerified = userService.verifyStudent(email);
        if (isVerified) {
            return new ResponseEntity<>(
                    new StandardResponse(200,
                            "Student was Verified!", true),
                    HttpStatus.OK
            );
        } else {
            return new ResponseEntity<>(
                    new StandardResponse(400,
                            "User is not an Student!", false),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("/get-user-details")
    @PreAuthorize("hasRole('user')")
    public ResponseEntity<StandardResponse> getUserDetails(
            @RequestHeader("Authorization") String tokenHeader
    ) {
        String token = tokenHeader.replace("Bearer ", "");
        String email = jwtService.getEmail(token);

        ResponseUserDetailsDto userDetails = userService.getUserDetails(email);

        return new ResponseEntity<>(
                new StandardResponse(200,
                        "user details!", userDetails),
                HttpStatus.OK
        );
    }


    @PostMapping("/create-trainer")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<StandardResponse> createTrainer(
            @RequestParam String email
    ) {
        String id = userService.createTrainer(email);
        return new ResponseEntity<>(
                new StandardResponse(201,
                        "Trainer was Verified!", id),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/create-student")
    @PreAuthorize("hasAnyRole('admin','user')")
    public ResponseEntity<StandardResponse> createStudent(
            @RequestParam String email
    ) {
        String id = userService.createStudent(email);
        return new ResponseEntity<>(
                new StandardResponse(201,
                        "student was Verified!", id),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/remove-trainer")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<StandardResponse> removeTrainer(
            @RequestParam String email
    ) {
        userService.removeTrainer(email);
        return new ResponseEntity<>(
                new StandardResponse(204,
                        "Trainer was removed!", null),
                HttpStatus.NO_CONTENT
        );
    }

    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<StandardResponse> getAllUsers(
            @RequestParam String searchText,
            @RequestParam int page,
            @RequestParam int size
    ) {
        return new ResponseEntity<>(
                new StandardResponse(200,
                        "Users List!", userService.findUsersPaginate(searchText, page, size)),
                HttpStatus.OK
        );
    }
    @GetMapping("/visitor/trusted-user-count")
    public ResponseEntity<StandardResponse> trustedUserCount() {
        return new ResponseEntity<>(
                new StandardResponse(200,
                        "Trusted User Count!",  userService.trustedUserCount()),
                HttpStatus.OK
        );
    }

    @GetMapping("/user/get-user-id")
    @PreAuthorize("hasRole('user')")
    public StandardResponse getUserId(
            @RequestHeader("Authorization") String tokenHeader
    ) {
        String token = tokenHeader.replace("Bearer ", "");
        String email = jwtService.getEmail(token);

        String userId = userService.getUserId(email);
        return new StandardResponse(200,
                        "user details!", userId);
    }

    @GetMapping("/user/get-user-email")
    @PreAuthorize("hasRole('user')")
    public StandardResponse getUserEmail(
            @RequestHeader("Authorization") String tokenHeader
    ) {
        String token = tokenHeader.replace("Bearer ", "");
        String email = jwtService.getEmail(token);
        return new StandardResponse(200,
                "user details!", email);
    }

    @GetMapping("/admin/get-user-id-by-email")
    @PreAuthorize("hasRole('admin')")
    public StandardResponse getUserIdByEmail(
            @RequestParam String email
    ) {


        String userId = userService.getUserId(email);

        return new StandardResponse(200,
                "user details!", userId);
    }

    @GetMapping("/teacher/check")
    @PreAuthorize("hasRole('teacher')")
    public StandardResponse check(
    ) {

        return new StandardResponse(200,
                "User have teacher privileges!", true);
    }

}

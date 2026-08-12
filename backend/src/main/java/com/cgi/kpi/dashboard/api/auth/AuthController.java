package com.cgi.kpi.dashboard.api.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cgi.kpi.dashboard.api.auth.dto.AuthMeResponseDto;
import com.cgi.kpi.dashboard.api.auth.dto.ChangePasswordRequestDto;
import com.cgi.kpi.dashboard.api.auth.dto.LoginRequestDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthMeResponseDto login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        return authService.login(request.username(), request.password(), httpRequest, httpResponse);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        authService.logout(httpRequest, httpResponse);
    }

    @GetMapping("/me")
    public AuthMeResponseDto me() {
        return authService.currentUser();
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @Valid @RequestBody ChangePasswordRequestDto request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        authService.changePassword(
                request.currentPassword(), request.newPassword(), httpRequest, httpResponse);
    }
}

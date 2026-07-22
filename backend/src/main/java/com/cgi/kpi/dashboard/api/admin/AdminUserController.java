package com.cgi.kpi.dashboard.api.admin;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cgi.kpi.dashboard.api.admin.dto.AdminResetPasswordRequestDto;
import com.cgi.kpi.dashboard.api.admin.dto.CreateUserRequestDto;
import com.cgi.kpi.dashboard.api.admin.dto.UpdateUserRequestDto;
import com.cgi.kpi.dashboard.api.admin.dto.UserAdminResponseDto;
import com.cgi.kpi.dashboard.domain.service.admin.AdminUserService;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public List<UserAdminResponseDto> listUsers() {
        return adminUserService.findAllUsers();
    }

    @PostMapping
    public UserAdminResponseDto createUser(@Valid @RequestBody CreateUserRequestDto request) {
        return adminUserService.createUser(request);
    }

    @PutMapping("/{id}")
    public UserAdminResponseDto updateUser(
            @PathVariable("id") UUID userId,
            @Valid @RequestBody UpdateUserRequestDto request) {
        return adminUserService.updateUser(userId, request);
    }

    @PutMapping("/{id}/password")
    public void resetPassword(
            @PathVariable("id") UUID userId,
            @Valid @RequestBody AdminResetPasswordRequestDto request) {
        adminUserService.resetPassword(userId, request);
    }
}

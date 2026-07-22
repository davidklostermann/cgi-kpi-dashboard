package com.cgi.kpi.dashboard.domain.service.admin;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cgi.kpi.dashboard.api.admin.dto.AdminResetPasswordRequestDto;
import com.cgi.kpi.dashboard.api.admin.dto.CreateUserRequestDto;
import com.cgi.kpi.dashboard.api.admin.dto.UpdateUserRequestDto;
import com.cgi.kpi.dashboard.api.admin.dto.UserAdminResponseDto;
import com.cgi.kpi.dashboard.api.error.ApiException;
import com.cgi.kpi.dashboard.domain.model.AppUser;
import com.cgi.kpi.dashboard.domain.model.WorkspaceIds;
import com.cgi.kpi.dashboard.domain.model.WorkspaceMembership;
import com.cgi.kpi.dashboard.domain.model.WorkspaceRole;
import com.cgi.kpi.dashboard.infrastructure.persistence.AppUserRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.WorkspaceMembershipRepository;
import com.cgi.kpi.dashboard.security.user.CurrentUserService;

@Service
public class AdminUserService {

    private static final Logger log = LoggerFactory.getLogger(AdminUserService.class);

    private final AppUserRepository appUserRepository;
    private final WorkspaceMembershipRepository workspaceMembershipRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    public AdminUserService(
            AppUserRepository appUserRepository,
            WorkspaceMembershipRepository workspaceMembershipRepository,
            PasswordEncoder passwordEncoder,
            CurrentUserService currentUserService) {
        this.appUserRepository = appUserRepository;
        this.workspaceMembershipRepository = workspaceMembershipRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<UserAdminResponseDto> findAllUsers() {
        currentUserService.requireAdmin();
        return appUserRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserAdminResponseDto createUser(CreateUserRequestDto request) {
        currentUserService.requireAdmin();
        
        if (appUserRepository.findByUsername(request.username()).isPresent()) {
            throw new ApiException("USER_ALREADY_EXISTS", "Username is already taken", HttpStatus.CONFLICT);
        }

        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setActive(true);
        user.setMustChangePassword(true);
        appUserRepository.saveAndFlush(user);

        WorkspaceMembership membership = new WorkspaceMembership();
        membership.setWorkspaceId(WorkspaceIds.DEFAULT);
        membership.setUserId(user.getId());
        membership.setRole(request.role());
        workspaceMembershipRepository.saveAndFlush(membership);

        log.info("USER_CREATED: Admin {} created user {} with role {}", 
            currentUserService.requireUserId(), user.getId(), request.role());

        return mapToDto(user);
    }

    @Transactional
    public UserAdminResponseDto updateUser(UUID userId, UpdateUserRequestDto request) {
        currentUserService.requireAdmin();
        
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));

        WorkspaceMembership membership = workspaceMembershipRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("MEMBERSHIP_NOT_FOUND", "User membership not found", HttpStatus.NOT_FOUND));

        // Last-Admin-Guard
        if (membership.getRole() == WorkspaceRole.ADMIN && user.isActive()) {
            if (!request.active() || request.role() != WorkspaceRole.ADMIN) {
                long otherAdmins = workspaceMembershipRepository.countActiveMembersWithRoleExcept(WorkspaceRole.ADMIN, userId);
                if (otherAdmins == 0) {
                    throw new ApiException("LAST_ADMIN_PROTECTION", "Cannot deactivate or downgrade the last active administrator", HttpStatus.BAD_REQUEST);
                }
            }
        }

        boolean activeChanged = user.isActive() != request.active();
        WorkspaceRole oldRole = membership.getRole();
        
        user.setActive(request.active());
        user.setMustChangePassword(request.mustChangePassword());
        appUserRepository.saveAndFlush(user);

        membership.setRole(request.role());
        workspaceMembershipRepository.saveAndFlush(membership);

        if (activeChanged) {
            log.info("USER_STATUS_CHANGED: Admin {} changed user {} active status to {}", 
                currentUserService.requireUserId(), userId, request.active());
        }
        if (oldRole != request.role()) {
            log.info("ROLE_CHANGED: Admin {} changed user {} role from {} to {}", 
                currentUserService.requireUserId(), userId, oldRole, request.role());
        }

        return mapToDto(user);
    }

    @Transactional
    public void resetPassword(UUID userId, AdminResetPasswordRequestDto request) {
        currentUserService.requireAdmin();

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(true);
        appUserRepository.saveAndFlush(user);

        log.info("USER_PASSWORD_RESET: Admin {} reset password for user {}", 
            currentUserService.requireUserId(), userId);
    }

    private UserAdminResponseDto mapToDto(AppUser user) {
        WorkspaceRole role = workspaceMembershipRepository.findByUserId(user.getId())
                .map(WorkspaceMembership::getRole)
                .orElse(WorkspaceRole.USER);
        
        return new UserAdminResponseDto(
            user.getId(),
            user.getUsername(),
            user.isActive(),
            role,
            user.isMustChangePassword(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}

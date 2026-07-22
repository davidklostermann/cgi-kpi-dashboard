package com.cgi.kpi.dashboard.security.user;

import java.util.List;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cgi.kpi.dashboard.domain.model.AppUser;
import com.cgi.kpi.dashboard.domain.model.WorkspaceIds;
import com.cgi.kpi.dashboard.domain.model.WorkspaceMembership;
import com.cgi.kpi.dashboard.domain.model.WorkspaceRole;
import com.cgi.kpi.dashboard.infrastructure.persistence.AppUserRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.WorkspaceMembershipRepository;

@Service
public class DashboardUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final WorkspaceMembershipRepository workspaceMembershipRepository;

    public DashboardUserDetailsService(
            AppUserRepository appUserRepository,
            WorkspaceMembershipRepository workspaceMembershipRepository) {
        this.appUserRepository = appUserRepository;
        this.workspaceMembershipRepository = workspaceMembershipRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = appUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new DisabledException("User is disabled");
        }

        WorkspaceMembership membership = workspaceMembershipRepository
                .findByWorkspaceIdAndUserId(WorkspaceIds.DEFAULT, user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Workspace membership not found"));

        return new DashboardUserDetails(
                user.getId(),
                membership.getWorkspaceId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.isActive(),
                user.isMustChangePassword(),
                List.of(new SimpleGrantedAuthority(toAuthority(membership.getRole()))));
    }

    static String toAuthority(WorkspaceRole role) {
        return "ROLE_" + role.name();
    }
}

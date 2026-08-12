package com.cgi.kpi.dashboard.security.user;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.test.context.support.WithSecurityContext;

/**
 * Test annotation that places a {@link DashboardUserDetails} principal in the security context
 * (Default Workspace). Replaces bare {@code @WithMockUser} for workspace-scoped endpoints.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@WithSecurityContext(factory = WithDashboardUserSecurityContextFactory.class)
public @interface WithDashboardUser {

    String username() default "test-admin";

    String role() default "ADMIN";

    boolean mustChangePassword() default false;
}

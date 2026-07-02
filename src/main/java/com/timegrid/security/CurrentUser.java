package com.timegrid.security;

import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public UUID userId() {
        return principal().getId();
    }

    public UUID organizationId() {
        return principal().getOrganizationId();
    }

    public List<String> roles() {
        return principal().getRoleNames();
    }

    public boolean hasRole(String role) {
        return principal().getRoleNames().contains(role);
    }

    private AppUserDetails principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (AppUserDetails) authentication.getPrincipal();
    }

}

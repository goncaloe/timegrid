package com.timegrid.security;

import com.timegrid.entity.Permission;
import com.timegrid.entity.Role;
import com.timegrid.entity.User;
import com.timegrid.enums.UserStatus;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.UUID;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class AppUserDetails implements UserDetails {

    private final UUID id;
    private final String username;
    private final String password;
    private final UUID organizationId;
    private final String organizationName;
    private final String fullName;
    private final UserStatus status;
    private final boolean softDeleted;
    private final List<String> roles;
    private final List<String> permissions;

    public AppUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPasswordHash();
        this.organizationId = user.getOrganization().getId();
        this.organizationName = user.getOrganization().getName();
        this.fullName = user.getFullName();
        this.status = user.getStatus();
        this.softDeleted = user.getDeletedAt() != null;
        this.roles = user.getRoles().stream()
                .map(Role::getName)
                .sorted()
                .toList();
        Set<String> perms = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());
        this.permissions = List.copyOf(perms);
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public List<String> getRoleNames() {
        return roles;
    }

    public List<String> getPermissionNames() {
        return permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        for (String permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE && !softDeleted;
    }

}
package com.timegrid.controller;

import com.timegrid.dto.PermissionResponse;
import com.timegrid.dto.RoleResponse;
import com.timegrid.service.AccessService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccessController {

    private final AccessService accessService;

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('user:read')")
    public List<RoleResponse> roles() {
        return accessService.findAllRoles().stream()
                .map(RoleResponse::from)
                .toList();
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('user:read')")
    public List<PermissionResponse> permissions() {
        return accessService.findAllPermissions().stream()
                .map(PermissionResponse::from)
                .toList();
    }

}

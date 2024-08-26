package com.fluxit.architecture.reference.authenticated.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api")
@Tag(name = "Endpoints")
@Slf4j
public class AuthenticatedController {

    @Operation(summary="Public endpoint")
    @GetMapping("/public")
    public String publicEndpoint() {
        return "This is a public endpoint";
    }

    @Operation(summary="Authenticated by any accessible endpoint")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/authenticated")
    public String authenticatedEndpoint(Principal principal) {
        log.info(principal.toString());
        return "This is an authenticated endpoint";
    }

    @Operation(summary="Authenticated by role user accessible endpoint")
    @PreAuthorize("hasRole('user')")
    @GetMapping("/user")
    public String userEndpoint(Principal principal) {
        log.info(principal.toString());
        return "This is an user-only endpoint";
    }

    @Operation(summary="Authenticated by role admin accessible endpoint")
    @PreAuthorize("hasRole('admin')")
    @GetMapping("/admin")
    public String adminEndpoint(Principal principal) {
        log.info(principal.toString());
        return "This is an admin-only endpoint";
    }
}
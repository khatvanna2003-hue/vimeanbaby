package com.vimeanbaby.security;

import com.vimeanbaby.user.entity.Role;

/** Authenticated principal stored in the security context. */
public record AuthUser(Long id, String email, Role role) {
}

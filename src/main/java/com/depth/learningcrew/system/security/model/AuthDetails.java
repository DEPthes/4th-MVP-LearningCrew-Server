package com.depth.learningcrew.system.security.model;

import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public abstract class AuthDetails implements AuthenticatedPrincipal {
    public abstract String getKey();

    public String getName() {
        return this.getKey();
    }

    public abstract Collection<? extends GrantedAuthority> getAuthorities();
}

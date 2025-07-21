package com.depth.learningcrew.system.security.model;

import com.depth.learningcrew.domain.user.entity.User;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public abstract class AuthDetails implements AuthenticatedPrincipal {
    public abstract String getKey();

    public String getName() {
        return this.getKey();
    }

    public abstract User getUser();

    public abstract Collection<? extends GrantedAuthority> getAuthorities();
}

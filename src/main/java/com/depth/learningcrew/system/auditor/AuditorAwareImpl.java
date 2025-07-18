package com.depth.learningcrew.system.auditor;

import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.system.security.model.UserDetails;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<User> {

    @Override
    @NonNull
    public Optional<User> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return Optional.ofNullable(userDetails.getUser());
        } else if (principal instanceof User) {
            return Optional.of((User) principal);
        }

        return Optional.empty();
    }
}

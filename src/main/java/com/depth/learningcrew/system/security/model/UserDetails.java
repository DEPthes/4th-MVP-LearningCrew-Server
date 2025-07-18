package com.depth.learningcrew.system.security.model;

import com.depth.learningcrew.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@RequiredArgsConstructor
@Getter
@Builder
public class UserDetails extends AuthDetails{
    private final User user;

    @Override
    public String getKey() {
        return user.getId();
    }

    public static UserDetails from(User user) {
        User unproxied = Hibernate.unproxy(user, User.class);
        // user.unproxy(); // 아직 Lazy Fetching이 필요하지 않으므로 unproxy 호출은 생략함

        return UserDetails.builder()
                .user(unproxied)
                .build();
    }

    public List<GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("User"));
    }
}
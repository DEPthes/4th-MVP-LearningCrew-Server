package com.depth.learningcrew.domain.user.service;

import com.depth.learningcrew.domain.user.dto.UserDto;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.domain.user.repository.UserRepository;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDto.UserUpdateResponse update(User user, UserDto.UserUpdateRequest request) {

        if (!user.getNickname().equals(request.getNickname())
                && userRepository.existsByNickname(request.getNickname())) {
            throw new RestException(ErrorCode.USER_NICKNAME_ALREADY_EXISTS);
        }

        request.applyTo(user, passwordEncoder);
        return UserDto.UserUpdateResponse.from(user);
    }
}

package com.depth.learningcrew.system.security.service;

import com.depth.learningcrew.domain.user.repository.UserRepository;
import com.depth.learningcrew.system.security.model.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserLoadServiceImpl implements UserLoadService {
    private final UserRepository userRepository;

    @Override
    public Optional<UserDetails> loadUserByKey(String key) {
        return userRepository.findById(Integer.parseInt(key))
                .map(UserDetails::from);
    }
}
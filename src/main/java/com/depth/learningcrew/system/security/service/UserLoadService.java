package com.depth.learningcrew.system.security.service;

import com.depth.learningcrew.system.security.model.AuthDetails;
import java.util.Optional;

public interface UserLoadService {
    Optional<? extends AuthDetails> loadUserByKey(String key);
}
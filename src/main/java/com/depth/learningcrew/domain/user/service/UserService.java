package com.depth.learningcrew.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.depth.learningcrew.domain.file.entity.ProfileImage;
import com.depth.learningcrew.domain.file.handler.FileHandler;
import com.depth.learningcrew.domain.file.repository.AttachedFileRepository;
import com.depth.learningcrew.domain.user.dto.UserDto;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.domain.user.repository.UserRepository;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileHandler fileHandler;
    private final AttachedFileRepository attachedFileRepository;

    @Transactional
    public UserDto.UserUpdateResponse update(User user, UserDto.UserUpdateRequest request) {

        User found = userRepository.findById(user.getId())
                .orElseThrow(() -> new RestException(ErrorCode.GLOBAL_NOT_FOUND));

        if (request.getNickname() != null &&
                !found.getNickname().equals(request.getNickname()) &&
                userRepository.existsByNickname(request.getNickname())) {
            throw new RestException(ErrorCode.USER_NICKNAME_ALREADY_EXISTS);
        }

        if (request.getEmail() != null &&
                !found.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new RestException(ErrorCode.USER_ALREADY_EMAIL_EXISTS);
        }

        request.applyTo(found, passwordEncoder);

        ProfileImage imageToSave = ProfileImage.from(request.getProfileImage());
        if (imageToSave != null) {
            imageToSave.setUser(found);

            fileHandler.saveFile(request.getProfileImage(), imageToSave);
            ProfileImage savedProfileImage = attachedFileRepository.save(imageToSave);

            if (found.getProfileImage() != null) {
                fileHandler.deleteFile(found.getProfileImage());
            }

            found.setProfileImage(savedProfileImage);
        }

        return UserDto.UserUpdateResponse.from(found);
    }
}

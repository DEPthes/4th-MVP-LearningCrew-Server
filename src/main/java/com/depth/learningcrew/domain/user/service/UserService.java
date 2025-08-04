package com.depth.learningcrew.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.depth.learningcrew.domain.file.entity.ProfileImage;
import com.depth.learningcrew.domain.file.handler.FileHandler;
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

        /* TODO: 로직 점검 (참새가 봉황의 뜻을 헤아릴 수 없기에 코드를 함부로 건드리지 않고 이렇게 남김)
        * 1. found에 setProfileImage를 하는데, user에도 setProfileImage를 하는 이유가 궁금
        * 2. ProfileImage가 연관관계 주인이면, profileImage.setUser(found) 가 있어야 하지 않나요?
        * 3. profileImageRepository.save(profileImage); 필요하지 않나요?
        *    PersistenceContext에 반영되지 않아서 profileImage가 영속화되지 않을 것 같아요.
        * */
        if (request.getProfileImage() != null) {
            if (found.getProfileImage() != null) {
                fileHandler.deleteFile(found.getProfileImage());
            }

            ProfileImage profileImage = ProfileImage.from(request.getProfileImage());
            // Suggest1. 연관관계 주인이면, profileImage.setUser(found) 가 있어야할 듯
            // profileImage.setUser(found);

            // Suggest2. profileImageRepository.save(profileImage); 필요할 것 같음
            // profileImageRepository.save(profileImage);

            fileHandler.saveFile(request.getProfileImage(), profileImage);
            found.setProfileImage(profileImage);
            user.setProfileImage(profileImage); // Suggest3. 이건 제거해도 될 것 같음
        }

        return UserDto.UserUpdateResponse.from(found);
    }
}

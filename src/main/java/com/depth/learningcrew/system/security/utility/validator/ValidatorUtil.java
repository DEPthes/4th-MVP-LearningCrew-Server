package com.depth.learningcrew.system.security.utility.validator;

import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class ValidatorUtil {
    public static void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new RestException(ErrorCode.GLOBAL_INVALID_PARAMETER, "이메일은 필수입니다.");
        }

        if (!Pattern.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$", email)) {
            throw new RestException(ErrorCode.GLOBAL_INVALID_PARAMETER, "이메일 형식이 올바르지 않습니다.");
        }
    }

    public static void validateNickname(String nickname) {
        if (!StringUtils.hasText(nickname)) {
            throw new RestException(ErrorCode.GLOBAL_INVALID_PARAMETER, "닉네임은 필수입니다.");
        }

        if (!Pattern.matches("^[ㄱ-ㅎ가-힣a-zA-Z0-9-_]{2,10}$", nickname)) {
            throw new RestException(ErrorCode.GLOBAL_INVALID_PARAMETER, "닉네임 조건에 부합하지 않습니다.");
        }
    }
}

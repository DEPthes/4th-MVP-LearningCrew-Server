package com.depth.learningcrew.system.exception.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    // Global
    GLOBAL_BAD_REQUEST(400, "올바르지 않은 요청입니다."),
    GLOBAL_NOT_FOUND(404, "요청한 사항을 찾을 수 없습니다."),
    GLOBAL_ALREADY_EXIST(400, "요청의 대상이 이미 존재합니다."),
    GLOBAL_METHOD_NOT_ALLOWED(405, "허용되지 않는 Method 입니다."),
    GLOBAL_INVALID_PARAMETER(400, "올바르지 않은 파라미터입니다."),
    INVALID_INPUT_VALUE(400, "유효하지 않은 입력 값입니다."),

    // Auth
    AUTH_PASSWORD_NOT_MATCH(401, "비밀번호가 올바르지 않습니다."),

    // User
    USER_ALREADY_JOINED(400, "이미 가입된 그룹입니다."),
    USER_NOT_FOUND(404, "존재하지 않는 사용자입니다."),
    USER_PASSWORD_NOT_MATCH(400, "올바른 비밀번호가 아닙니다."),
    USER_NICKNAME_ALREADY_EXISTS(409, "중복되는 닉네임입니다."),
    USER_ALREADY_EMAIL_EXISTS(409, "중복되는 아이디입니다."),

    // Invite
    INVITE_ALREADY_MEMBER(400, "이미 등록된 멤버입니다."),
    INVITE_CODE_EXPIRED(400, "만료된 초대 코드입니다."),
    INVITE_SELF_INVITATION_NOT_ALLOWED(400, "스스로를 초대할 수 없습니다."),
    INVITE_ALREADY_SENT(400, "이미 초대장이 발송되었습니다."),

    // jwt
    AUTH_TOKEN_NOT_FOUND(401, "인증 토큰을 찾을 수 없습니다."),
    AUTH_TOKEN_EXPIRED(401, "토큰이 만료되었습니다."),
    AUTH_TOKEN_INVALID(401, "올바른 토큰이 아닙니다."),
    AUTH_TOKEN_MALFORMED(401, "토큰 형식이 올바르지 않습니다."),
    AUTH_AUTHENTICATION_FAILED(401, "인증에 실패했습니다."),
    AUTH_USER_NOT_FOUND(404, "등록된 유저를 찾을 수 없습니다."),
    AUTH_FORBIDDEN(403, "접근 권한이 없습니다."),
    AUTH_CANNOT_GENERATE_TOKEN(400, "인증키를 생성 할 수 없습니다."),
    AUTH_TOKEN_MISSING(401, "JWT 토큰이 누락되었습니다."),

    // File
    FILE_ALREADY_EXISTS(400, "파일이 이미 존재합니다."),
    FILE_NOT_FOUND(404, "파일을 찾을 수 없습니다."),
    FILE_NOT_IMAGE(400, "이미지 파일이 아닙니다."),

    // Comment
    PARENT_COMMENT_NOT_FOUND(404, "부모 댓글을 찾을 수 없습니다"),
    COMMENT_NOT_FOUND(404, "댓글을 찾을 수 없습니다"),
    COMMENT_NOT_USER_TYPE(403, "사용자 댓글만 수정/삭제할 수 있습니다."),

    // Study Group
    STUDY_GROUP_ALREADY_MEMBER(400, "이미 스터디 그룹의 멤버입니다."),
    STUDY_GROUP_ALREADY_APPLIED(400, "이미 가입 신청한 스터디 그룹입니다."),
    STUDY_GROUP_APPLICATION_ALREADY_APPROVED(400, "이미 수락된 신청입니다."),
    STUDY_GROUP_APPLICATION_ALREADY_REJECTED(400, "이미 거절된 신청입니다."),
    STUDY_GROUP_NOT_FOUND(404, "스터디 그룹을 찾을 수 없습니다."),
    STUDY_GROUP_OWNER_CANNOT_BE_EXPELLED(400, "스터디 그룹의 소유자는 추방할 수 없습니다."),
    STUDY_GROUP_NOT_CURRENT_STEP(400, "현재 스텝에서는 질문을 생성할 수 없습니다."),
    STUDY_GROUP_NOT_MEMBER(403, "스터디 그룹의 멤버가 아닙니다."),
    STUDY_GROUP_STEP_NOT_WRITABLE(400, "현재 진행 중인 스텝에서만 작성할 수 있습니다."),
    STUDY_GROUP_STEP_DUPLICATE_DATE(400, "중복된 스텝 날짜가 있습니다."),
    STUDY_GROUP_STEP_END_DATE_MISMATCH(400, "스텝의 마지막 날짜와 종료 날짜가 일치하지 않습니다."),

    // Group Category
    GROUP_CATEGORY_ALREADY_EXIST(400, "중복되는 카테고리명입니다."),
    GROUP_CATEGORY_NOT_FOUND(404, "그룹 카테고리를 찾을 수 없습니다."),

    // Q&A
    QANDA_NOT_FOUND(404, "질문을 찾을 수 없습니다."),
    QANDA_NOT_AUTHORIZED(403, "질문을 수정할 권한이 없습니다."),

    // Note
    NOTE_NOT_FOUND(404, "노트를 찾을 수 없습니다."),
    NOTE_NOT_AUTHORIZED(403, "노트를 수정할 권한이 없습니다."),
    NOTE_ALREADY_EXISTS_IN_STEP(409, "해당 스텝에 이미 노트가 존재합니다."),

    // Quiz
    QUIZ_NOT_FOUND(404, "퀴즈를 찾을 수 없습니다."),
    QUIZ_ALREADY_SUBMITTED_IN_STEP(409, "해당 스텝의 답변을 이미 제출했습니다."),

    // Other
    INTERNAL_SERVER_ERROR(500, "오류가 발생했습니다."),;

    private final int statusCode;
    private final String message;
}

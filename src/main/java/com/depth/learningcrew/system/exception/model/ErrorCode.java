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

    // File
    FILE_ALREADY_EXISTS(400, "파일이 이미 존재합니다."),
    FILE_NOT_FOUND(404, "파일을 찾을 수 없습니다."),
    FILE_NOT_IMAGE(400, "이미지 파일이 아닙니다."),

    // DiaryBook, Diary
    DIARYBOOK_NOT_FOUND(404, "일기장을 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(404, "멤버를 찾을 수 없습니다."),
    DIARY_NOT_FOUND(404, "일기를 찾을 수 없습니다."),

    STICKER_NOT_FOUND(404, "스티커를 찾을 수 없습니다."),

    REACTION_ALREADY_EXISTS(400, "이미 공감 한 일기입니다."),
    REACTION_NOT_FOUND(404, "존재하지 않는 공감입니다."),

    DIARYBOOK_ANALYSIS_NOT_FOUND(404, "일기장 분석 결과를 찾을 수 없습니다."),

    // Comment
    PARENT_COMMENT_NOT_FOUND(404, "부모 댓글을 찾을 수 없습니다"),
    COMMENT_NOT_FOUND(404, "댓글을 찾을 수 없습니다"),
    COMMENT_NOT_USER_TYPE(403, "사용자 댓글만 수정/삭제할 수 있습니다."),

    // AI
    AI_IMAGE_CREATION_FAILED(500, "AI 이미지 생성에 실패했습니다."),
    AI_CHARACTER_NOT_FOUND(404, "AI 캐릭터를 찾을 수 없습니다"),
    AI_CHARACTER_NOT_CUSTOM(403, "커스텀 AI 캐릭터만 수정/삭제가 가능합니다"),
    AI_MUSIC_CREATION_FAILED(500, "AI 음악 생성에 실패했습니다."),
    AI_MUSIC_SERVER_BUSY(429, "현재 다른 음악 생성 요청이 처리 중입니다. 잠시 후 다시 시도해주세요."),
    AI_MUSIC_JOB_NOT_FOUND(404, "음악 생성 작업을 찾을 수 없습니다."),
    AI_MUSIC_JOB_FAILED(500, "음악 생성 작업이 실패했습니다."),
    AI_MUSIC_DOWNLOAD_FAILED(500, "음악 파일 다운로드에 실패했습니다."),
    AI_NODE_NOT_FOUND(404, "AI 노드를 찾을 수 없습니다."),
    AI_NODE_URL_ALREADY_EXISTS(400, "이미 등록된 AI 노드 URL입니다."),

    // DiaryBookMember
    MEMBER_ALREADY_ADMIN(400, "이미 관리자인 멤버입니다."),
    MEMBER_NOT_ADMIN(400, "일반 멤버입니다"),

    // Sticker
    STICKER_IMAGE_NOT_FOUND(404, "임시 업로드된 스티커 이미지를 찾을 수 없습니다."),

    // Other
    INTERNAL_SERVER_ERROR(500, "오류가 발생했습니다."), ;

    private final int statusCode;
    private final String message;
}

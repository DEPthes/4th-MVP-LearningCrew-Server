package com.depth.learningcrew.domain.auth.token.dto;

import com.depth.learningcrew.domain.auth.token.entity.RefreshToken;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class RefreshTokenDto {
    private String uuid;
    private String userKey;
    private LocalDateTime expiryDate;

    public static RefreshToken toEntity(
            String uuid,
            String userKey,
            LocalDateTime expiryDate
    ) {
        return RefreshToken.builder()
                .uuid(uuid)
                .userKey(userKey)
                .expiryDate(expiryDate)
                .build();
    }
}

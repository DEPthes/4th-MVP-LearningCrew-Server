package com.depth.learningcrew.domain.ai.llm.dto;

import lombok.Data;

import java.util.List;

@Data
public class OptionsPayload {
    private List<Opt> options;

    @Data
    public static class Opt {
        private String id;           // 질문 Item id와 매핑
        private List<String> choices; // 4개
        private Integer answerIndex;  // 0~3
    }
}

package com.depth.learningcrew.domain.ai.llm.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuizzesPayload {
    private List<Item> quizzes;

    @Data
    public static class Item {
        private String id;
        private String stem;
        private String answer;
    }
}

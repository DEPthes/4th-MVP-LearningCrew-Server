package com.depth.learningcrew.domain.ai.llm.service;

import com.depth.learningcrew.domain.ai.llm.dto.QuizzesPayload;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface QuizGenerator {
    @SystemMessage("""
        ** Your Persona **
        You are a Professor and quiz writer.

        ** Role **
        Produce EXACTLY 20 diverse, factual quizzes
        strictly based on the provided notes. Do NOT invent facts.

        ** Task **
        Output STRICT JSON ONLY (no markdown):
        {
          "quizzes": [
            {"id":"Q1","stem":"...","answer":"..."},
            ...
            {"id":"Q20","stem":"...","answer":"..."}
          ]
        }

        ** Rules **
        - 20 unique quizzes.
        - Each question must have a single correct answer (answer field).
        - Cover different topics across notes.
        - Keep stems concise.
    """)
    @UserMessage("""
        Study Group: {{groupName}}
        Step: {{step}}

        ===== Notes (title + content aggregated) =====
        {{notes}}
    """)
    QuizzesPayload generate(
            @V("groupName") String groupName,
            @V("step") int step,
            @V("notes") String notes
    );
}

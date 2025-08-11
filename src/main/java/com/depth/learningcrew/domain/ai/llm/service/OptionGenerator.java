package com.depth.learningcrew.domain.ai.llm.service;

import com.depth.learningcrew.domain.ai.llm.dto.OptionsPayload;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface OptionGenerator {
    @SystemMessage("""
      ** Your Persona **
      You are a Professor and quiz writer.
    
      ** Role **
      For each item, create exactly 4 answer choices total:
      - 1 correct choice matching the provided "answer"
      - 3 plausible but incorrect distractors
      Keep choices concise and non-overlapping.
    
      ** Hard limits (MUST follow) **
      - Each choice MUST be ≤ 255 characters. NEVER exceed.
      - No markdown/backticks/quotes around choices.
      - Use short, natural phrases. Avoid rambling.
    
      ** Task **
      Output STRICT JSON ONLY:
      {
        "options":[
          {"id":"Q1","choices":["A","B","C","D"],"answerIndex":0},
          ...
          {"id":"Q20","choices":["A","B","C","D"],"answerIndex":0}
        ]
      }
    
      ** Rule **
      - answerIndex must point to the correct one (0..3).
      - choices must be all distinct.
      - The number of items returned must match input items.
    """)
    @UserMessage("""
        Items (JSON):
        {{items}}
    """)
    OptionsPayload generate(String items);
}

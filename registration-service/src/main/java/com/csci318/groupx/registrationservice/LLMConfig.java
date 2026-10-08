package com.csci318.groupx.registrationservice;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LLMConfig {

    @Bean
    public LLMInterface registrationAssistant() {
        // 1. Initialize your model
        ChatModel model = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("gpt-4o-mini")
                .build();

        // 2. Setup memory
        MessageWindowChatMemory memory = MessageWindowChatMemory.withMaxMessages(10);

        // 3. Build and return the service bean
        return AiServices.builder(LLMInterface.class)
                .chatModel(model)
                .chatMemory(memory)
                .build();
    }
}
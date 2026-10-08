package com.csci318.groupx.registrationservice;

import dev.langchain4j.service.SystemMessage;

public interface LLMInterface {

    @SystemMessage("placeholder")
    String chat(String userMessage);
}
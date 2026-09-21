package com.example.homework.controller;

import com.example.homework.common.R;
import com.example.homework.constant.AiConstants;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /*@PostMapping("/chat")
    public R<String> chat(@RequestBody String message) {
        String reply = chatClient.prompt(message).call().content();
        return R.success(reply);
    }*/
    // 流式接口
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody String msg) {
        return chatClient.prompt(msg)
                .system(AiConstants.HOMEWORK_TEACHER_PROMPT)
                .stream()
                .content();
    }
}
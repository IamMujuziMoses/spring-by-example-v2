package com.springbyexample.v2.springintegrationtesting.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.springintegrationtesting.Message;
import com.springbyexample.v2.springintegrationtesting.service.MessageService;

/**
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Message createMessage(@RequestBody CreateMessageRequest request) {
        return messageService.createMessage(request.content());
    }

    @GetMapping("/{id}")
    public Message getMessage(@PathVariable Long id) {
        return messageService.getMessage(id);
    }

    /**
     * Represents the request body used to create a message.
     *
     * @param content the message content
     */
    public record CreateMessageRequest(String content) {
    }
}

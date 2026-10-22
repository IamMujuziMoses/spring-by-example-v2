package com.springbyexample.v2.springtestcontainers.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.springtestcontainers.Message;
import com.springbyexample.v2.springtestcontainers.service.MessageService;

/**
 * Exposes HTTP endpoints for creating and retrieving messages.
 *
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
     * Represents a request to create a message.
     *
     * @param content the message content
     */
    public record CreateMessageRequest(String content) {
    }
}
package com.springbyexample.v2.springtestcontainers.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springbyexample.v2.springtestcontainers.Message;
import com.springbyexample.v2.springtestcontainers.exception.MessageNotFoundException;
import com.springbyexample.v2.springtestcontainers.repository.MessageRepository;

/**
 * Provides business operations for messages.
 *
 * @author Mujuzi Moses
 */
@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Transactional
    public Message createMessage(String content) {
        return messageRepository.save(new Message(content));
    }

    @Transactional(readOnly = true)
    public Message getMessage(Long id) {
        return messageRepository.findById(id).orElseThrow(() -> new MessageNotFoundException(id));
    }
}

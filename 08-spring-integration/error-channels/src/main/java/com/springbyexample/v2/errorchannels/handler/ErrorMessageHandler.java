package com.springbyexample.v2.errorchannels.handler;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.stereotype.Component;

import com.springbyexample.v2.errorchannels.model.ErrorDetails;
import com.springbyexample.v2.errorchannels.support.ErrorProcessingSupport;

/**
 * Handles errors published to the dedicated order error channel.
 *
 * @author Mujuzi Moses
 */
@Component
public class ErrorMessageHandler {

    private final ErrorProcessingSupport errorProcessingSupport;

    private final BlockingQueue<ErrorDetails> handledErrors;

    /**
     * Creates an error message handler.
     *
     * @param errorProcessingSupport support for extracting error details
     */
    @Autowired
    public ErrorMessageHandler(ErrorProcessingSupport errorProcessingSupport) {
        this(errorProcessingSupport, new LinkedBlockingQueue<>());
    }

    /**
     * Creates an error message handler with a custom error queue.
     *
     * @param errorProcessingSupport support for extracting error details
     * @param handledErrors queue for storing handled errors
     */
    public ErrorMessageHandler(ErrorProcessingSupport errorProcessingSupport, BlockingQueue<ErrorDetails> handledErrors) {
        this.errorProcessingSupport = errorProcessingSupport;
        this.handledErrors = handledErrors;
    }

    /**
     * Handles an error message received through the order error channel.
     *
     * @param errorMessage error message to handle
     */
    @ServiceActivator(inputChannel = "orderErrorChannel")
    public void handle(ErrorMessage errorMessage) {
        ErrorDetails details = errorProcessingSupport.process(errorMessage);

        if (!handledErrors.offer(details)) {
            throw new IllegalStateException("Failed to add error details to the queue");
        }
    }

    /**
     * Waits for an error to be handled.
     *
     * @param timeout maximum time to wait
     * @return handled error details, or null if no error arrives
     * @throws InterruptedException if the waiting thread is interrupted
     */
    public ErrorDetails awaitError(Duration timeout) throws InterruptedException {
        return handledErrors.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Removes previously handled errors.
     */
    public void clear() {
        handledErrors.clear();
    }
}

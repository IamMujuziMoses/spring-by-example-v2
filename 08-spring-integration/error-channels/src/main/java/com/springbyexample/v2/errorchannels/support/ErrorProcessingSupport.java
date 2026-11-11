package com.springbyexample.v2.errorchannels.support;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.stereotype.Component;

import com.springbyexample.v2.errorchannels.model.ErrorDetails;
import com.springbyexample.v2.errorchannels.model.OrderMessage;

/**
 * Extracts and validates details from Spring Integration error messages.
 *
 * @author Mujuzi Moses
 */
@Component
public class ErrorProcessingSupport {

    /**
     * Extracts details from an error message.
     *
     * @param message message to inspect
     * @return extracted error details
     */
    public ErrorDetails process(Message<?> message) {
        if (!(message instanceof ErrorMessage errorMessage)) {
            throw new IllegalArgumentException("Expected a Spring Integration ErrorMessage");
        }

        Throwable exception = errorMessage.getPayload();
        Message<?> failedMessage = errorMessage.getOriginalMessage();

        if (failedMessage == null && exception instanceof MessagingException messagingException) {
            failedMessage = messagingException.getFailedMessage();
        }

        if (failedMessage == null || !(failedMessage.getPayload() instanceof OrderMessage order)) {
            throw new IllegalArgumentException("ErrorMessage must contain an original OrderMessage");
        }

        Throwable rootCause = getRootCause(exception);

        return new ErrorDetails(order, rootCause.getClass().getSimpleName(), rootCause.getMessage());
    }

    /**
     * Finds the deepest cause of an exception.
     *
     * @param exception exception to inspect
     * @return root cause
     */
    private Throwable getRootCause(Throwable exception) {
        Throwable cause = exception;

        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }

        return cause;
    }
}

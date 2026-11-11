package com.springbyexample.v2.errorchannels.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.messaging.support.MessageBuilder;

import com.springbyexample.v2.errorchannels.handler.ErrorMessageHandler;
import com.springbyexample.v2.errorchannels.model.ErrorDetails;
import com.springbyexample.v2.errorchannels.model.OrderMessage;

/**
 * Unit tests for error message processing.
 *
 * @author Mujuzi Moses
 */
public class ErrorProcessingSupportTest {

    private ErrorProcessingSupport errorProcessingSupport;

    @BeforeEach
    void setUp() {
        errorProcessingSupport = new ErrorProcessingSupport();
    }

    @Test
    void process_shouldExtractOrderAndExceptionDetails() {
        OrderMessage order = new OrderMessage("ORDER-001", 0);
        Message<OrderMessage> originalMessage = MessageBuilder.withPayload(order).build();
        IllegalArgumentException exception = new IllegalArgumentException("Order quantity must be greater than zero");
        ErrorMessage errorMessage = new ErrorMessage(exception, originalMessage);
        ErrorDetails details = errorProcessingSupport.process(errorMessage);

        assertEquals(order, details.failedOrder());
        assertEquals("IllegalArgumentException", details.exceptionType());
        assertEquals("Order quantity must be greater than zero", details.exceptionMessage());
    }

    @Test
    void process_shouldExtractFailedMessageFromMessagingException() {
        OrderMessage order = new OrderMessage("ORDER-002", 0);
        Message<OrderMessage> originalMessage = MessageBuilder.withPayload(order).build();
        IllegalArgumentException cause = new IllegalArgumentException("Order quantity must be greater than zero");
        MessageHandlingException exception = new MessageHandlingException(originalMessage, "Order processing failed", cause);
        ErrorMessage errorMessage = new ErrorMessage(exception);
        ErrorDetails details = errorProcessingSupport.process(errorMessage);

        assertEquals(order, details.failedOrder());
        assertEquals("IllegalArgumentException", details.exceptionType());
        assertEquals("Order quantity must be greater than zero", details.exceptionMessage());
    }

    @Test
    void process_shouldUnwrapRootCause() {
        OrderMessage order = new OrderMessage("ORDER-003", 0);
        Message<OrderMessage> originalMessage = MessageBuilder.withPayload(order).build();
        IllegalArgumentException rootCause = new IllegalArgumentException("Order quantity must be greater than zero");
        IllegalStateException wrapperException = new IllegalStateException("Unexpected processing failure", rootCause);
        MessageHandlingException exception = new MessageHandlingException(originalMessage, "Order processing failed",
                wrapperException);
        ErrorMessage errorMessage = new ErrorMessage(exception);
        ErrorDetails details = errorProcessingSupport.process(errorMessage);

        assertEquals(order, details.failedOrder());
        assertEquals("IllegalArgumentException", details.exceptionType());
        assertEquals("Order quantity must be greater than zero", details.exceptionMessage());
    }

    @Test
    void process_shouldRejectErrorMessageWithoutOrderMessage() {
        Message<String> originalMessage = MessageBuilder.withPayload("Unsupported payload").build();
        ErrorMessage errorMessage = new ErrorMessage(new IllegalArgumentException("Processing failed"), originalMessage);

        assertThrows(IllegalArgumentException.class, () -> errorProcessingSupport.process(errorMessage));
    }

    @Test
    void process_shouldRejectNonErrorMessage() {
        OrderMessage order = new OrderMessage("ORDER-004", 1);
        Message<OrderMessage> message = MessageBuilder.withPayload(order).build();

        assertThrows(IllegalArgumentException.class, () -> errorProcessingSupport.process(message));
    }

    @Test
    void process_shouldRejectErrorMessageWithoutOriginalMessage() {
        ErrorMessage errorMessage = new ErrorMessage(new IllegalArgumentException("Processing failed"));

        assertThrows(IllegalArgumentException.class, () -> errorProcessingSupport.process(errorMessage));
    }

    @Test
    void process_shouldRejectErrorMessageWithUnsupportedPayload() {
        Message<String> originalMessage = MessageBuilder.withPayload("Unsupported payload").build();

        ErrorMessage errorMessage = new ErrorMessage(new IllegalArgumentException("Processing failed"), originalMessage);

        assertThrows(IllegalArgumentException.class, () -> errorProcessingSupport.process(errorMessage));
    }

    @Test
    void process_shouldExtractDeepestRootCause() {
        OrderMessage order = new OrderMessage("ORDER-005", 0);
        Message<OrderMessage> originalMessage = MessageBuilder.withPayload(order).build();
        IllegalArgumentException rootCause = new IllegalArgumentException("Order quantity must be greater than zero");
        IllegalStateException intermediateException = new IllegalStateException("Validation failed", rootCause);
        RuntimeException outerException = new RuntimeException("Order processing failed", intermediateException);
        MessageHandlingException exception = new MessageHandlingException(originalMessage, "Message handling failed",
                outerException);
        ErrorMessage errorMessage = new ErrorMessage(exception);
        ErrorDetails details = errorProcessingSupport.process(errorMessage);

        assertEquals("IllegalArgumentException", details.exceptionType());
        assertEquals("Order quantity must be greater than zero", details.exceptionMessage());
    }

    @Test
    void process_shouldHandleExceptionWithoutCause() {
        OrderMessage order = new OrderMessage("ORDER-006", 0);
        Message<OrderMessage> originalMessage = MessageBuilder.withPayload(order).build();
        IllegalArgumentException exception = new IllegalArgumentException("Order quantity must be greater than zero");
        ErrorMessage errorMessage = new ErrorMessage(exception, originalMessage);
        ErrorDetails details = errorProcessingSupport.process(errorMessage);

        assertEquals("IllegalArgumentException", details.exceptionType());
        assertEquals("Order quantity must be greater than zero", details.exceptionMessage());
    }

    @Test
    void process_shouldHandleExceptionWhoseCauseReferencesItself() {
        OrderMessage order = new OrderMessage("ORDER-007", 0);
        Message<OrderMessage> originalMessage = MessageBuilder.withPayload(order).build();
        SelfReferencingException exception = new SelfReferencingException("Order quantity must be greater than zero");
        ErrorMessage errorMessage = new ErrorMessage(exception, originalMessage);
        ErrorDetails details = errorProcessingSupport.process(errorMessage);

        assertEquals("SelfReferencingException", details.exceptionType());
        assertEquals("Order quantity must be greater than zero", details.exceptionMessage());
    }

    @Test
    void handle_shouldThrowIllegalStateExceptionWhenQueueIsFull() {
        BlockingQueue<ErrorDetails> handledErrors = new ArrayBlockingQueue<>(1);
        handledErrors.add(new ErrorDetails(new OrderMessage("ORDER-EXISTING", 1),
                "IllegalArgumentException", "Existing error"));
        ErrorProcessingSupport errorProcessingSupport = mock(ErrorProcessingSupport.class);
        ErrorMessageHandler handler = new ErrorMessageHandler(errorProcessingSupport, handledErrors);
        ErrorMessage errorMessage = mock(ErrorMessage.class);
        ErrorDetails details = new ErrorDetails(new OrderMessage("ORDER-001", 0),
                "IllegalArgumentException", "Order quantity must be greater than zero");

        when(errorProcessingSupport.process(errorMessage)).thenReturn(details);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> handler.handle(errorMessage));

        assertEquals("Failed to add error details to the queue", exception.getMessage());
        assertEquals(1, handledErrors.size());
    }

    private static class SelfReferencingException extends RuntimeException {

        private SelfReferencingException(String message) {
            super(message);
        }

        @Override
        public synchronized Throwable getCause() {
            return this;
        }
    }
}

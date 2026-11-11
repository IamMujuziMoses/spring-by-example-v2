package com.springbyexample.v2.errorchannels.model;

/**
 * Contains details about a failed order message.
 *
 * @param failedOrder      original order that failed processing
 * @param exceptionType    type of exception raised during processing
 * @param exceptionMessage exception message
 *
 * @author Mujuzi Moses
 */
public record ErrorDetails(OrderMessage failedOrder, String exceptionType, String exceptionMessage) {
}

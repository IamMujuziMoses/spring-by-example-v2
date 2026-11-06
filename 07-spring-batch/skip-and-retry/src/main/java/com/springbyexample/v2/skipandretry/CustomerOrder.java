package com.springbyexample.v2.skipandretry;

/**
 * Represents an order submitted for batch processing.
 *
 * @param id                             the order identifier
 * @param description                    the order description
 * @param invalid                        whether the order contains invalid data
 * @param transientFailuresBeforeSuccess the number of transient failures
 *                                       before processing can succeed
 * @author Mujuzi Moses
 */
public record CustomerOrder(long id, String description, boolean invalid, int transientFailuresBeforeSuccess) {
}

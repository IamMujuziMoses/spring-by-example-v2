package com.springbyexample.v2.batchtesting.domain;

/**
 * Represents an order to be processed by a batch job.
 *
 * @param id          order identifier
 * @param description order description
 * @author Mujuzi Moses
 */
public record Order(long id, String description) {
}

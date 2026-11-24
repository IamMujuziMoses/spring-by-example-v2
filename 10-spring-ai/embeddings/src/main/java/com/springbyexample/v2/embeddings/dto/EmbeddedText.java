package com.springbyexample.v2.embeddings.dto;

import java.util.List;

/**
 * Represents a text and its generated embedding vector.
 *
 * @author Mujuzi Moses
 */
public record EmbeddedText(String text, int dimensions, List<Float> vector) {
}

package com.springbyexample.v2.embeddings.dto;

import java.util.List;

/**
 * Response containing the generated embeddings.
 *
 * @author Mujuzi Moses
 */
public record EmbeddingResponse(List<EmbeddedText> embeddings) {
}

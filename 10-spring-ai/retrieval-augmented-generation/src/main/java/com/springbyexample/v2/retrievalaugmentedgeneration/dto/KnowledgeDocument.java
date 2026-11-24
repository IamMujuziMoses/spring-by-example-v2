package com.springbyexample.v2.retrievalaugmentedgeneration.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents a knowledge document supplied for retrieval.
 *
 * @param content  the document content
 * @param metadata additional document metadata
 *
 * @author Mujuzi Moses
 */
public record KnowledgeDocument(@NotBlank String content, Map<String, Object> metadata) {
}

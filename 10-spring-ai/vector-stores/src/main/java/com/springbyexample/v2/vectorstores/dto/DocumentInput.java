package com.springbyexample.v2.vectorstores.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents a document submitted for vector storage.
 *
 * @param content  document content
 * @param metadata optional document metadata
 *
 * @author Mujuzi Moses
 */
public record DocumentInput(@NotBlank String content, Map<String, Object> metadata) {
}

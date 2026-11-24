package com.springbyexample.v2.vectorstores.dto;

import java.util.Map;

/**
 * Represents a document retrieved from a vector store.
 *
 * @param id       document identifier
 * @param content  document content
 * @param metadata document metadata
 *
 * @author Mujuzi Moses
 */
public record VectorSearchResult(String id, String content, Map<String, Object> metadata) {
}

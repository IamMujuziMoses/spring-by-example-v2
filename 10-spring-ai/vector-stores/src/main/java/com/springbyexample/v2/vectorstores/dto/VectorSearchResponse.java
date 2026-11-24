package com.springbyexample.v2.vectorstores.dto;

import java.util.List;

/**
 * Response containing documents retrieved through semantic search.
 *
 * @param query   original search query
 * @param results matching documents
 *
 * @author Mujuzi Moses
 */
public record VectorSearchResponse(String query, List<VectorSearchResult> results) {
}

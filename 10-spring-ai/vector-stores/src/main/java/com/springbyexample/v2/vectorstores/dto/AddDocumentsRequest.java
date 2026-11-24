package com.springbyexample.v2.vectorstores.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

/**
 * Request containing documents to add to the vector store.
 *
 * @param documents documents to store
 *
 * @author Mujuzi Moses
 */
public record AddDocumentsRequest(@NotEmpty List<@Valid DocumentInput> documents) {
}

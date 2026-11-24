package com.springbyexample.v2.retrievalaugmentedgeneration.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

/**
 * Request containing documents to add to the knowledge base.
 *
 * @param documents the documents to store
 *
 * @author Mujuzi Moses
 */
public record AddKnowledgeRequest(@NotEmpty List<@Valid KnowledgeDocument> documents) {
}

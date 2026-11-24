package com.springbyexample.v2.retrievalaugmentedgeneration.dto;

/**
 * Response returned after documents are added to the knowledge base.
 *
 * @param documentsAdded the number of documents added
 *
 * @author Mujuzi Moses
 */
public record AddKnowledgeResponse(int documentsAdded) {
}

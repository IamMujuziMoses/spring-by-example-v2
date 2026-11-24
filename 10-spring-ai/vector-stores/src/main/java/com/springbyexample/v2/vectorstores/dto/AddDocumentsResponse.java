package com.springbyexample.v2.vectorstores.dto;

/**
 * Response returned after documents have been added.
 *
 * @param documentsAdded number of documents added
 *
 * @author Mujuzi Moses
 */
public record AddDocumentsResponse(int documentsAdded) {
}

package com.springbyexample.v2.embeddings.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import com.springbyexample.v2.embeddings.dto.EmbeddedText;
import com.springbyexample.v2.embeddings.dto.EmbeddingResponse;

/**
 * Service responsible for generating text embeddings.
 *
 * @author Mujuzi Moses
 */
@Service
public class EmbeddingsService {

    private final EmbeddingModel embeddingModel;

    public EmbeddingsService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public EmbeddingResponse generateEmbeddings(List<String> texts) {
        List<float[]> vectors = embeddingModel.embed(texts);

        if (vectors == null || vectors.size() != texts.size()) {
            throw new IllegalStateException("The embedding model returned an invalid response.");
        }

        List<EmbeddedText> embeddings = new java.util.ArrayList<>();

        for (int i = 0; i < texts.size(); i++) {
            float[] vector = vectors.get(i);

            if (vector == null || vector.length == 0) {
                throw new IllegalStateException("The embedding model returned an empty embedding.");
            }

            List<Float> values = new ArrayList<>(vector.length);

            for (float value : vector) {
                values.add(value);
            }

            embeddings.add(new EmbeddedText(texts.get(i), vector.length, values));
        }

        return new EmbeddingResponse(List.copyOf(embeddings));
    }
}

package similarityAnalyzer;

import java.util.*;

public class DocumentSimilarity {

    // return score(cosineSimilarity): calculating score between two stings (using TF-IDF algorithm, with Cosine similarity score)
    public double calculateMatchingScore(String documentContent1, String documentContent2) {

        List<Map<String, Double>> documentTermVectors = getTFIDFVectors(Arrays.asList(documentContent1, documentContent2));

        Map<String, Double> document1Vector = documentTermVectors.get(0);
        Map<String, Double> document2Vector = documentTermVectors.get(1);

        return calculateCosineSimilarity(document1Vector, document2Vector);
    }

    private static List<Map<String, Double>> getTFIDFVectors(List<String> documents) {
        List<Map<String, Double>> tfidfVectors = new ArrayList<>();

        // Step 1: Calculate the term frequency (TF) for each document
        List<Map<String, Integer>> termFrequencyVectors = new ArrayList<>();
        for (String document : documents) {
            termFrequencyVectors.add(getTermFrequencyVector(document));
        }

        // Step 2: Calculate the inverse document frequency (IDF) for each term
        Map<String, Double> inverseDocumentFrequencyVector = getInverseDocumentFrequencyVector(termFrequencyVectors);

        // Step 3: Calculate the TF-IDF vector for each document
        for (Map<String, Integer> termFrequencyVector : termFrequencyVectors) {
            Map<String, Double> tfidfVector = new HashMap<>();
            for (Map.Entry<String, Integer> entry : termFrequencyVector.entrySet()) {
                String term = entry.getKey();
                int frequency = entry.getValue();
                double tfidf = frequency * inverseDocumentFrequencyVector.get(term);
                tfidfVector.put(term, tfidf);
            }
            tfidfVectors.add(tfidfVector);
        }

        return tfidfVectors;
    }

    private static Map<String, Integer> getTermFrequencyVector(String document) {
        Map<String, Integer> termFrequencyVector = new HashMap<>();
        String[] terms = document.split(" ");
        for (String term : terms) {
            termFrequencyVector.put(term, termFrequencyVector.getOrDefault(term, 0) + 1);
        }
        return termFrequencyVector;
    }

    private static Map<String, Double> getInverseDocumentFrequencyVector(List<Map<String, Integer>> termFrequencyVectors) {
        Map<String, Double> inverseDocumentFrequencyVector = new HashMap<>();
        int numDocuments = termFrequencyVectors.size();

        for (Map<String, Integer> termFrequencyVector : termFrequencyVectors) {
            for (String term : termFrequencyVector.keySet()) {
                inverseDocumentFrequencyVector.put(term, inverseDocumentFrequencyVector.getOrDefault(term, 0.0) + 1.0);
            }
        }

        for (String term : inverseDocumentFrequencyVector.keySet()) {
            double idf = Math.log(numDocuments / (1.0 + inverseDocumentFrequencyVector.get(term)));
            inverseDocumentFrequencyVector.put(term, idf);
        }

        return inverseDocumentFrequencyVector;
    }

    private static double calculateCosineSimilarity(Map<String, Double> vector1, Map<String, Double> vector2) {
        double dotProduct = 0;
        double magnitude1 = 0;
        double magnitude2 = 0;

        // Calculate the dot product and magnitudes of the vectors
        for (String term : vector1.keySet()) {
            double value1 = vector1.get(term);
            double value2 = vector2.getOrDefault(term, 0.0);

            dotProduct += value1 * value2;
            magnitude1 += value1 * value1;
        }

        for (String term : vector2.keySet()) {
            double value2 = vector2.get(term);
            magnitude2 += value2 * value2;
        }

        // Calculate the cosine similarity score
        double denominator = Math.sqrt(magnitude1) * Math.sqrt(magnitude2);
        if (denominator == 0) {
            return 0;
        } else {
            return dotProduct / denominator;
        }
    }
}

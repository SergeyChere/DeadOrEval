package ai.never.trust;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

public class App {
    
    static final String MODEL = "llama3.2:3b";
    static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    static final int TOTAL = 1000;

    static final String CONTEXT = "Client called a dental clinic and said: " +
        "I am not sure I will be free on Friday. " +
        "Tuesday is a good day but worse than Friday. " +
        "Tuesday is better than Wednesday. " +
        "I am busy on Wednesday.";

    static final String CHATBOT_QUESTION = "Which day would you like to schedule your appointment?";
    static final String CHATBOT_ANSWER = "I will book you in for Tuesday.";
    static final String EXPECTED_DAY = "Tuesday";

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        List<Double> scores = new ArrayList<>();
        int failed = 0;

        System.out.println("Running " + TOTAL + " evaluations...\n");

        for (int i = 1; i <= TOTAL; i++) {
            System.out.println("[" + i + "] Sending request...");
            String rawResponse = sendRequest(client);
            System.out.println("[" + i + "] Raw response: " + rawResponse);

            String modelText = extractResponse(rawResponse);
            System.out.println("[" + i + "] Model said: " + modelText);

            Double score = parseScore(modelText);
            if (score != null) {
                scores.add(score);
                System.out.println("[" + i + "] Score: " + score);
            } else {
                failed++;
                System.out.println("[" + i + "] Score: FAILED TO PARSE");
            }
            System.out.println("---");
        }

        System.out.println("\n=== RESULTS ===");
        System.out.println("Total evaluated: " + scores.size());
        System.out.println("Failed to parse: " + failed);

        if (!scores.isEmpty()) {
            double avg = scores.stream().mapToDouble(d -> d).average().orElse(0);
            double min = scores.stream().mapToDouble(d -> d).min().orElse(0);
            double max = scores.stream().mapToDouble(d -> d).max().orElse(0);
            System.out.println("Average score:   " + String.format("%.3f", avg));
            System.out.println("Min score:       " + min);
            System.out.println("Max score:       " + max);
        }
    }

    static String sendRequest(HttpClient client) throws Exception {
        String judgePrompt = "You are a strict factual judge. " +
            "You will receive a Question and an Answer. " +
            "Your job is to evaluate if the Answer is factually correct. " +
            "Respond ONLY with a single decimal number: " +
            "1.0 means the Answer is 100% correct. " +
            "0.0 means the Answer is 100% wrong. " +
            "0.5 means the Answer is partially correct. " +
            "No words. No explanation. Just the number.\\n" +

            "Context: " + CONTEXT + "\\n" +
            "Chatbot question: " + CHATBOT_QUESTION + "\\n" +
            "Chatbot answer: " + CHATBOT_ANSWER + "\\n" +
            "Expected correct day: " + EXPECTED_DAY;
        
        String requestBody = "{\"model\":\"" + MODEL + "\",\"prompt\":\"" + judgePrompt + "\",\"stream\":false}";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(OLLAMA_URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        HttpResponse<String> response = client.send(request,
            HttpResponse.BodyHandlers.ofString());
        
        return response.body();
    }

    static String extractResponse(String rawResponse) {
        try {
            int start = rawResponse.indexOf("\"response\":\"") + 12;
            int end = rawResponse.indexOf("\"", start);
            return rawResponse.substring(start, end);
        } catch (Exception e) {
            return "";
        }
    }

    static Double parseScore(String text) {
        Pattern pattern = Pattern.compile("\\d+\\.\\d+|\\d+");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            double score = Double.parseDouble(matcher.group());
            if (score > 1.0) score = score / 10.0;
            return score;
        }
        return null;
    }
}
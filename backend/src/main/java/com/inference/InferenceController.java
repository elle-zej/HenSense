package com.inference;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono; // Add this import
import org.springframework.http.HttpStatusCode; // Add this import

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api/inference")
public class InferenceController {

    private final WebClient webClient;

    public InferenceController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8000").build(); // FastAPI server URL
    }

    // ✅ 1️⃣ Handles image URLs from frontend
    @PostMapping("/classify-frontend")
    public ResponseEntity<String> classifyImageFromFrontend(@RequestParam("url") String imageUrl) {
        return sendImageUrlToModel(imageUrl);
    }

    // ✅ 2️⃣ Allows testing with a local image file or other use cases
    @PostMapping("/classify")
    public ResponseEntity<String> classifyImage(@RequestBody Map<String, String> request) {
        try {
            // Log the incoming request
            System.out.println("Received request: " + request);

            // Validate the URL
            String imageUrl = request.get("url");
            if (imageUrl == null || imageUrl.isEmpty()) {
                return ResponseEntity.badRequest().body("{\"error\": \"URL is required\"}");
            }

            // Log the image URL
            System.out.println("Received image URL: " + imageUrl);

            // Send the URL to the FastAPI model
            return sendImageUrlToModel(imageUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // 🔹 3️⃣ Helper method to send image URL to FastAPI model
    private ResponseEntity<String> sendImageUrlToModel(String imageUrl) {
        try {
            // Log the request being sent to FastAPI
            System.out.println("Sending request to FastAPI with URL: " + imageUrl);

            // Send image URL to FastAPI model
            String response = webClient.post()
                    .uri("/predict/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"url\": \"" + imageUrl + "\"}")
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), clientResponse -> {
                        // Log 4xx errors
                        System.out.println("Client error: " + clientResponse.statusCode());
                        return Mono.error(new RuntimeException("Client error: " + clientResponse.statusCode()));
                    })
                    .onStatus(status -> status.is5xxServerError(), clientResponse -> {
                        // Log 5xx errors
                        System.out.println("Server error: " + clientResponse.statusCode());
                        return Mono.error(new RuntimeException("Server error: " + clientResponse.statusCode()));
                    })
                    .bodyToMono(String.class)
                    .block(); // Blocking call for simplicity

            // Log the response from FastAPI
            System.out.println("Received response from FastAPI: " + response);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log the exception
            System.out.println("Error sending request to FastAPI: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // 🔹 4️⃣ Helper method to host a local image file and return its URL
    private String hostLocalImage(String imagePath) throws IOException {
        // For simplicity, assume the image is hosted locally at a known URL
        // In a real application, you would need to host the file using a static file server
        return "http://localhost:8000/temp/" + Path.of(imagePath).getFileName().toString();
    }
}
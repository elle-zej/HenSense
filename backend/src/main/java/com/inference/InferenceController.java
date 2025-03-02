package com.inference;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/inference")
public class InferenceController {

    private final WebClient webClient;

    public InferenceController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8000").build(); // FastAPI server URL
    }

    // ✅ 1️⃣ Handles image URLs from frontend
    @PostMapping("/classify")
    public ResponseEntity<String> classifyChicken(@RequestParam("url") String imageUrl) {
        return sendImageUrlToModel(imageUrl);
    }

    // ✅ 2️⃣ Allows testing with a local image file (hosted locally)
    @PostMapping("/classify-local")
    public ResponseEntity<String> classifyLocalImage(@RequestParam("path") String imagePath) throws IOException {
        // Host the local image file and get its URL
        String imageUrl = hostLocalImage(imagePath);
        return sendImageUrlToModel(imageUrl);
    }

    // 🔹 3️⃣ Helper method to send image URL to FastAPI model
    private ResponseEntity<String> sendImageUrlToModel(String imageUrl) {
        // Send image URL to FastAPI model
        String response = webClient.post()
                .uri("/predict/")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"url\": \"" + imageUrl + "\"}")
                .retrieve()
                .bodyToMono(String.class)
                .block(); // Blocking call for simplicity

        return ResponseEntity.ok(response);
    }

    // 🔹 4️⃣ Helper method to host a local image file and return its URL
    private String hostLocalImage(String imagePath) throws IOException {
        // For simplicity, assume the image is hosted locally at a known URL
        // In a real application, you would need to host the file using a static file server
        return "http://localhost:8000/temp/" + Path.of(imagePath).getFileName().toString();
    }
}
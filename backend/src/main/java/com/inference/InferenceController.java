import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;

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

    // ✅ 1️⃣ This method handles file uploads from frontend (Existing)
    @PostMapping("/classify")
    public ResponseEntity<String> classifyChicken(@RequestParam("file") MultipartFile file) throws IOException {
        return sendImageToModel(file.getBytes(), file.getOriginalFilename());
    }

    // ✅ 2️⃣ This method allows testing with a local image file
    @PostMapping("/classify-local")
    public ResponseEntity<String> classifyLocalImage(@RequestParam("path") String imagePath) throws IOException {
        byte[] imageBytes = Files.readAllBytes(Path.of(imagePath)); // Read local image file
        return sendImageToModel(imageBytes, "local_image.jpg");
    }

    // 🔹 3️⃣ Helper method to send image bytes to FastAPI model
    private ResponseEntity<String> sendImageToModel(byte[] imageBytes, String filename) {
        // Prepare image as multipart data
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return filename; // Simulate a file name
            }
        };
        body.add("file", imageResource);

        // Send image to FastAPI model
        String response = webClient.post()
                .uri("/predict/")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block(); // Blocking call for simplicity

        return ResponseEntity.ok(response);
    }
}

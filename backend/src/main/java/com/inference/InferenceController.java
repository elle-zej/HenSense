@RestController
@RequestMapping("/api/inference")
public class InferenceController {

    private final WebClient webClient;

    public InferenceController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8000").build(); // FastAPI server URL
    }

    @PostMapping("/classify")
    public ResponseEntity<String> classifyChicken(@RequestParam("file") MultipartFile file) throws IOException {
        // Convert image to byte array
        byte[] imageBytes = file.getBytes();

        // Send image to FastAPI model server
        String response = webClient.post()
                .uri("/predict/")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(new MultipartBodyBuilder().part("file", imageBytes).build())
                .retrieve()
                .bodyToMono(String.class)
                .block(); // Blocking call for simplicity

        return ResponseEntity.ok(response);
    }
}

package phishing_detector.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnalysisRequest(

        @Size(max = 320, message = "sender must be at most 320 characters")
        String sender,

        @Size(max = 500, message = "subject must be at most 500 characters")
        String subject,

        @NotBlank(message = "message is required")
        @Size(max = 5000, message = "message must be at most 5000 characters")
        String message
) {
}
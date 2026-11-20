package phishing_detector.dto;

public record FieldErrorResponse(
        String field,
        String message
) {
}
package phishing_detector.dto;

public record SignalResponse(
        String type,
        String source,
        String sourceField,
        String description,
        String evidence
) {
}
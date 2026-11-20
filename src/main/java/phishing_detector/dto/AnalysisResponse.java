package phishing_detector.dto;

import phishing_detector.model.RiskLevel;

import java.time.Instant;
import java.util.List;

public record AnalysisResponse(
        Long id,
        Instant createdAt,
        RiskLevel riskLevel,
        String riskReason,
        String aiExplanation,
        String recommendedAction,
        List<SignalResponse> signals,
        String disclaimer,
        boolean simulated
) {
    public static final String DISCLAIMER =
            "This is a risk assessment, not a confirmation of fraud. "
            + "A low risk level does not guarantee the message is safe.";
}
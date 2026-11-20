package phishing_detector.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import phishing_detector.dto.AnalysisRequest;
import phishing_detector.dto.AnalysisResponse;
import phishing_detector.model.RiskLevel;

@RestController 
@RequestMapping ("/api/analyses")
public class AnalysisController {


@PostMapping 
 public ResponseEntity<AnalysisResponse> analyze(@Valid @RequestBody AnalysisRequest request) {
    // SIMULATED: no real analysis yet.
    AnalysisResponse response = new AnalysisResponse(
        1L,
        Instant.now(),
        RiskLevel.INCONCLUSIVE,
        "[SIMULATED] No rules were applied.",
        "[SIMULATED] No AI analysis was performed.",
        "[SIMULATED] Placeholder data. Do not rely on this result.",
        List.of(),
        AnalysisResponse.DISCLAIMER,
        true
    );
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

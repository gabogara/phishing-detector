package phishing_detector.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalysisController.class)
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void validRequestReturns201WithSimulatedResult() throws Exception {
        String body = """
                {
                  "sender": "support@secure-bank-verification.com",
                  "subject": "URGENT: your account will be locked",
                  "message": "Verify your identity within 24 hours."
                }
                """;

        mockMvc.perform(post("/api/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.riskLevel").value("INCONCLUSIVE"))
                .andExpect(jsonPath("$.signals").isEmpty())
                .andExpect(jsonPath("$.simulated").value(true))
                .andExpect(jsonPath("$.disclaimer").isNotEmpty());
    }

    @Test
    void blankMessageReturns400WithFieldError() throws Exception {
        String body = """
                { "message": "   " }
                """;

        mockMvc.perform(post("/api/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.length()").value(1))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("message"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("message is required"));
    }

    @Test
    void severalInvalidFieldsAreReturnedSortedByField() throws Exception {
        String body = """
                { "sender": "%s", "message": "" }
                """.formatted("a".repeat(321));

        mockMvc.perform(post("/api/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()").value(2))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("message"))
                .andExpect(jsonPath("$.fieldErrors[1].field").value("sender"));
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"message\": \"Hello\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is missing or is not valid JSON"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void unsupportedMethodReturns405() throws Exception {
        mockMvc.perform(get("/api/analyses"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void nonJsonContentTypeReturns415() throws Exception {
        mockMvc.perform(post("/api/analyses")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("hello"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));
    }
}
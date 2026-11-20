package phishing_detector.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private List<String> errorsFor(AnalysisRequest request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getMessage)
                .toList();
    }

    @Test
    void validRequestHasNoErrors() {
        AnalysisRequest request = new AnalysisRequest("a@b.com", "Hello", "Click here");

        assertThat(errorsFor(request)).isEmpty();
    }

    @Test
    void senderAndSubjectAreOptional() {
        AnalysisRequest request = new AnalysisRequest(null, null, "Click here");

        assertThat(errorsFor(request)).isEmpty();
    }

    @Test
    void nullMessageIsRejected() {
        AnalysisRequest request = new AnalysisRequest(null, null, null);

        assertThat(errorsFor(request)).containsExactly("message is required");
    }

    @Test
    void blankMessageIsRejected() {
        AnalysisRequest request = new AnalysisRequest(null, null, "   ");

        assertThat(errorsFor(request)).containsExactly("message is required");
    }

    @Test
    void messageOfExactly5000CharactersIsAccepted() {
        AnalysisRequest request = new AnalysisRequest(null, null, "a".repeat(5000));

        assertThat(errorsFor(request)).isEmpty();
    }

    @Test
    void messageLongerThan5000CharactersIsRejected() {
        AnalysisRequest request = new AnalysisRequest(null, null, "a".repeat(5001));

        assertThat(errorsFor(request)).containsExactly("message must be at most 5000 characters");
    }

    @Test
    void senderLongerThan320CharactersIsRejected() {
        AnalysisRequest request = new AnalysisRequest("a".repeat(321), null, "Click here");

        assertThat(errorsFor(request)).containsExactly("sender must be at most 320 characters");
    }
}
package phishing_detector.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import phishing_detector.dto.ErrorResponse;
import phishing_detector.dto.FieldErrorResponse;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        List<FieldErrorResponse> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
                .sorted(Comparator.comparing(FieldErrorResponse::field))
                .toList();

        return buildError(HttpStatus.BAD_REQUEST, "Validation failed", request, fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        return buildError(HttpStatus.BAD_REQUEST,
                "Request body is missing or is not valid JSON", request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOtherErrors(
            Exception exception,
            HttpServletRequest request) {

        // Known Spring errors (405, 404, 415...) already carry the right status.
        if (exception instanceof org.springframework.web.ErrorResponse springError) {
            HttpStatusCode status = springError.getStatusCode();
            String detail = springError.getBody().getDetail();
            log.warn("Request error {} on {} {}",
                    status.value(), request.getMethod(), request.getRequestURI());
            return buildError(status,
                    detail != null ? detail : "The request could not be processed",
                    request, List.of());
        }

        // Anything else is a bug: full details go to the log only, never to the client.
        log.error("Unexpected error on {} {}",
                request.getMethod(), request.getRequestURI(), exception);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred", request, List.of());
    }

    private ResponseEntity<ErrorResponse> buildError(
            HttpStatusCode status,
            String message,
            HttpServletRequest request,
            List<FieldErrorResponse> fieldErrors) {

        HttpStatus knownStatus = HttpStatus.resolve(status.value());
        String reason = knownStatus != null ? knownStatus.getReasonPhrase() : "Error";

        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                status.value(),
                reason,
                message,
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity.status(status).body(body);
    }
}
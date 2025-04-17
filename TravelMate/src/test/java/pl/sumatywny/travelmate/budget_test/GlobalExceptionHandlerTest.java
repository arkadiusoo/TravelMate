package pl.sumatywny.travelmate.budget_test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import pl.sumatywny.travelmate.config.GlobalExceptionHandler;
import pl.sumatywny.travelmate.config.NotFoundException;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidationErrors_shouldReturnBadRequestWithFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("objectName", "field1", "must not be null");
        FieldError fieldError2 = new FieldError("objectName", "field2", "must be valid");

        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException((MethodParameter) null, bindingResult);

        ResponseEntity<Map<String, String>> response = handler.handleValidationErrors(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("field1", "must not be null")
                                       .containsEntry("field2", "must be valid");
    }

    @Test
    void handleConstraintViolation_shouldReturnBadRequestWithViolationDetails() {
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);

        Path path1 = mock(Path.class);
        Path path2 = mock(Path.class);

        when(path1.toString()).thenReturn("property1");
        when(path2.toString()).thenReturn("property2");

        when(violation1.getPropertyPath()).thenReturn(path1);
        when(violation1.getMessage()).thenReturn("must not be null");

        when(violation2.getPropertyPath()).thenReturn(path2);
        when(violation2.getMessage()).thenReturn("must be valid");

        Set<ConstraintViolation<?>> violations = new HashSet<>(Set.of(violation1, violation2));
        ConstraintViolationException ex = new ConstraintViolationException(violations);

        ResponseEntity<Map<String, String>> response = handler.handleConstraintViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("property1", "must not be null")
                                       .containsEntry("property2", "must be valid");
    }

    @Test
    void handleNotFound_shouldReturnNotFoundWithErrorMessage() {
        NotFoundException ex = new NotFoundException("Resource not found");

        ResponseEntity<Map<String, String>> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("error", "Resource not found");
    }
}
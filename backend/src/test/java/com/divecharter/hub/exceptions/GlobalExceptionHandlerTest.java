package com.divecharter.hub.exceptions;

import com.divecharter.hub.dto.CommonDtos.ErrorResponse;
import com.divecharter.hub.models.DiveTrip;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");

    private void assertError(ResponseEntity<ErrorResponse> response, int status, String messagePart) {
        assertThat(response.getStatusCode().value()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(status);
        assertThat(response.getBody().message()).contains(messagePart);
        assertThat(response.getBody().path()).isEqualTo("/api/test");
    }

    @Test
    void notFound_returns404() {
        assertError(handler.handleNotFound(new ResourceNotFoundException("Dive site", 99L), request),
                404, "Dive site not found with id 99");
    }

    @Test
    void duplicate_returns409() {
        assertError(handler.handleDuplicate(new DuplicateResourceException("already exists"), request),
                409, "already exists");
    }

    @Test
    void apiException_usesItsOwnStatus() {
        assertError(handler.handleApiException(new TripFullException("Reef Runner"), request),
                409, "fully booked");
        assertError(handler.handleApiException(new CertificationInsufficientException("too deep"), request),
                422, "too deep");
        assertError(handler.handleApiException(new CertificationNotVerifiedException(), request),
                403, "not been verified");
    }

    @Test
    void validation_returns400WithFieldErrors() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "Email is required"));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, request);

        assertError(response, 400, "Validation failed");
        assertThat(response.getBody().fieldErrors()).containsEntry("email", "Email is required");
    }

    @Test
    void badParameter_returns400() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getValue()).thenReturn("abc");
        when(ex.getName()).thenReturn("id");

        assertError(handler.handleTypeMismatch(ex, request), 400, "Invalid value 'abc' for parameter 'id'");
    }

    @Test
    void unreadableBody_returns400() {
        assertError(handler.handleUnreadable(mock(HttpMessageNotReadableException.class), request),
                400, "Malformed JSON");
    }

    @Test
    void unknownEndpoint_returns404() {
        assertError(handler.handleNoEndpoint(mock(NoResourceFoundException.class), request),
                404, "No endpoint at /api/test");
    }

    @Test
    void wrongMethod_returns405() {
        ResponseEntity<ErrorResponse> response =
                handler.handleMethodNotAllowed(new HttpRequestMethodNotSupportedException("DELETE"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(405);
    }

    @Test
    void badLogin_returns401() {
        assertError(handler.handleAuthentication(new BadCredentialsException("bad"), request),
                401, "Invalid email or password");
    }

    @Test
    void accessDenied_returns403() {
        assertError(handler.handleAccessDenied(new AccessDeniedException("no"), request),
                403, "permission");
    }

    @Test
    void dataIntegrity_returns409() {
        assertError(handler.handleDataIntegrity(new DataIntegrityViolationException("fk"), request),
                409, "in use");
    }

    @Test
    void optimisticLock_returns409() {
        assertError(handler.handleOptimisticLock(
                        new ObjectOptimisticLockingFailureException(DiveTrip.class, 1L), request),
                409, "updated by another booking");
    }

    @Test
    void unexpectedError_returns500_withoutLeakingDetails() {
        ResponseEntity<ErrorResponse> response =
                handler.handleUnexpected(new RuntimeException("secret internal detail"), request);

        assertError(response, 500, "An unexpected error occurred");
        assertThat(response.getBody().message()).doesNotContain("secret internal detail");
    }
}
package com.divecharter.hub.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Shapes shared by every feature: paginated lists and errors. */
public final class CommonDtos {

    private CommonDtos() {
    }

    /** The same JSON shape for every paginated list. */
    public record PageResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean last
    ) {
        public static <T> PageResponse<T> from(Page<T> page) {
            return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                    page.getTotalElements(), page.getTotalPages(), page.isLast());
        }
    }

    /** The single error shape every endpoint returns. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {
        public static ErrorResponse of(HttpStatus status, String message, String path) {
            return of(status, message, path, null);
        }

        public static ErrorResponse of(HttpStatus status, String message, String path,
                                       Map<String, String> fieldErrors) {
            return new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(),
                    message, path, fieldErrors);
        }
    }
}
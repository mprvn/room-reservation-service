package com.marvel.hospitality.common;

import com.marvel.hospitality.payment.creditcard.exception.CreditCardPaymentNotFoundException;
import com.marvel.hospitality.payment.creditcard.exception.PaymentServiceUnavailableException;
import com.marvel.hospitality.reservation.service.ReservationExceptions;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Validation failed", "Request validation failed");
        problem.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        return ResponseEntity.badRequest().body(problem(HttpStatus.BAD_REQUEST, "Malformed request",
                "Request body is missing or malformed "));
    }
    @ExceptionHandler(ReservationExceptions.InvalidReservationException.class)
    public ProblemDetail handleInvalidReservation(ReservationExceptions.InvalidReservationException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid reservation", ex.getMessage());
    }

    @ExceptionHandler(ReservationExceptions.ReservationNotFoundException.class)
    public ProblemDetail handleNotFound(ReservationExceptions.ReservationNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Reservation not found", ex.getMessage());
    }

    @ExceptionHandler(ReservationExceptions.RoomNotAvailableException.class)
    public ProblemDetail handleRoomNotAvailable(ReservationExceptions.RoomNotAvailableException ex) {
        return problem(HttpStatus.CONFLICT, "Room not available", ex.getMessage());
    }

    @ExceptionHandler({ReservationExceptions.CreditCardPaymentNotConfirmedException.class, CreditCardPaymentNotFoundException.class})
    public ProblemDetail handlePaymentNotConfirmed(RuntimeException ex) {
        return problem(HttpStatus.valueOf(422), "Credit card payment not confirmed", ex.getMessage());
    }

    @ExceptionHandler(PaymentServiceUnavailableException.class)
    public ProblemDetail handlePaymentServiceUnavailable(PaymentServiceUnavailableException ex) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Payment service unavailable",
                "Credit card payment status could not be verified, please retry later");
    }

    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleUnexpected(RuntimeException ex) {
        log.error("Unexpected error", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "An unexpected error occurred");
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}

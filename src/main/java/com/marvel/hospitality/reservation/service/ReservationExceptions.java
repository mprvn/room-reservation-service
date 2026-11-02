package com.marvel.hospitality.reservation.service;

import com.marvel.hospitality.payment.creditcard.model.CreditCardPaymentStatus;

public final class ReservationExceptions {

    private ReservationExceptions() {
    }

    /** The request is syntactically valid but violates a business rule. */
    public static class InvalidReservationException extends RuntimeException {
        public InvalidReservationException(String message) {
            super(message);
        }
    }

    public static class RoomNotAvailableException extends RuntimeException {
        public RoomNotAvailableException(String roomNumber) {
            super("Room %s is not available for the requested period".formatted(roomNumber));
        }
    }

    public static class CreditCardPaymentNotConfirmedException extends RuntimeException {
        public CreditCardPaymentNotConfirmedException(String paymentReference, CreditCardPaymentStatus status) {
            super("Credit card payment '%s' is not confirmed (status: %s)".formatted(paymentReference, status));
        }
    }

    public static class ReservationNotFoundException extends RuntimeException {
        public ReservationNotFoundException(String reservationId) {
            super("Reservation '%s' was not found".formatted(reservationId));
        }
    }
}

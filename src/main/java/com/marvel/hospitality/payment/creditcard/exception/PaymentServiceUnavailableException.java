package com.marvel.hospitality.payment.creditcard.exception;

/** The credit-card-payment-service could not be reached or returned an unusable answer. */
public class PaymentServiceUnavailableException extends RuntimeException {

    public PaymentServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.marvel.hospitality.payment.creditcard.exception;

public class CreditCardPaymentNotFoundException extends RuntimeException {

    public CreditCardPaymentNotFoundException(String paymentReference) {
        super("Credit card payment with reference '%s' was not found".formatted(paymentReference));
    }
}

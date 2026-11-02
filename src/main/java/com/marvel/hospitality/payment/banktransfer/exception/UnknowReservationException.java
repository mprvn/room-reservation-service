package com.marvel.hospitality.payment.banktransfer.exception;

public class UnknowReservationException extends RuntimeException{

    public UnknowReservationException(String reservationId, String paymentId){
        super("Payment %s references unknown reservation %s".formatted(paymentId, reservationId));
    }
}

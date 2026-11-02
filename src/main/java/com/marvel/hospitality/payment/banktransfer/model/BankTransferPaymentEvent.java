package com.marvel.hospitality.payment.banktransfer.model;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;

/**
 *
 * @param paymentId Unique id of the payment
 * @param debtorAccountNumber Account number from which payment was done
 * @param amountReceived  Payment Amount received
 * @param transactionDescription Transaction description format
 *       format: E2E unique id(10 character) + reservationId (8 characters)
 */
public record BankTransferPaymentEvent(
        String paymentId,
        @JsonAlias("debtorAccountnumber") String debtorAccountNumber,
        BigDecimal amountReceived,
        String transactionDescription) {
}

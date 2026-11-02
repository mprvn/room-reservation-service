package com.marvel.hospitality.payment.creditcard.service;

import com.marvel.hospitality.openapi.creditcard.model.PaymentStatusResponse;
import com.marvel.hospitality.openapi.creditcard.model.PaymentStatusRetrievalRequest;
import com.marvel.hospitality.payment.creditcard.exception.CreditCardPaymentNotFoundException;
import com.marvel.hospitality.payment.creditcard.exception.PaymentServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Client for credit-card-payment-service ({@code POST /payment-status}).
 */
public class CreditCardPaymentClient {

    private static final Logger log = LoggerFactory.getLogger(CreditCardPaymentClient.class);

    private final RestClient restClient;

    public CreditCardPaymentClient(RestClient restClient) {
        this.restClient = restClient;
    }


    public PaymentStatusResponse retrievePaymentStatus(String paymentReference) {
        try {
            PaymentStatusResponse response = restClient.post()
                    .uri("/payment-status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(new PaymentStatusRetrievalRequest(paymentReference))
                    .retrieve()
                    .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(), (request, resp) -> {
                        throw new CreditCardPaymentNotFoundException(paymentReference);
                    })
                    .body(PaymentStatusResponse.class);

            if (response == null || response.getStatus() == null) {
                throw new PaymentServiceUnavailableException(
                        "credit-card-payment-service returned no payment status", null);
            }
            log.info("Credit card payment {} has status {}", paymentReference, response.getStatus());
            return response;
        } catch (CreditCardPaymentNotFoundException | PaymentServiceUnavailableException e) {
            throw e;
        } catch (RestClientException e) {
            log.error("Failed to retrieve credit card payment status for {}", paymentReference, e);
            throw new PaymentServiceUnavailableException(
                    "Unable to retrieve credit card payment status: " + e.getMessage(), e);
        }
    }
}

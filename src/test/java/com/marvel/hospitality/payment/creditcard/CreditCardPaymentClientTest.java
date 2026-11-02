package com.marvel.hospitality.payment.creditcard;

import com.marvel.hospitality.openapi.creditcard.model.PaymentStatusResponse;
import com.marvel.hospitality.payment.creditcard.model.CreditCardPaymentStatus;
import com.marvel.hospitality.payment.creditcard.service.CreditCardPaymentClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@TestPropertySource( locations = "classpath:application-test.yml")
public class CreditCardPaymentClientTest {
    private static final String BASE_URL = "http://localhost:9090/host/credit-card-payment-api";

    private MockRestServiceServer server;
    private CreditCardPaymentClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new CreditCardPaymentClient(builder.build());
    }

    @Test
    void returnsConfirmedStatus() {
        server.expect(requestTo(BASE_URL + "/payment-status"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"paymentReference\":\"DL123456789\"}"))
                .andRespond(withSuccess("{\"lastUpdateDate\":\"2017-07-21T17:32:28Z\",\"status\":\"CONFIRMED\"}",
                        MediaType.APPLICATION_JSON));

        PaymentStatusResponse response = client.retrievePaymentStatus("DL123456789");

        assertThat(response.getStatus()).isEqualTo(CreditCardPaymentStatus.CONFIRMED.name());
        assertThat(response.getLastUpdateDate()).isNotNull();
        server.verify();
    }
    @Test
    void returnsRejectedStatus() {
        server.expect(requestTo(BASE_URL + "/payment-status"))
                .andRespond(withSuccess("{\"status\":\"REJECTED\"}", MediaType.APPLICATION_JSON));

        assertThat(client.retrievePaymentStatus("DL123456789").getStatus())
                .isEqualTo(CreditCardPaymentStatus.REJECTED.name());
    }
}

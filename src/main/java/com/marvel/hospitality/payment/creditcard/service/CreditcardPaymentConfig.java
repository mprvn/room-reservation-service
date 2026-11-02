package com.marvel.hospitality.payment.creditcard.service;

import com.marvel.hospitality.common.AppProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
public class CreditcardPaymentConfig {

    /**
     *
     * @param builder passing updated base url via the rest client
     * @param properties app properties form application.yml
     * @return builder
     */
    @Bean
    public RestClient creditCardPaymentRestClient(RestClient.Builder builder, AppProperties properties) {
        AppProperties.CreditCardPayment config = properties.creditCardPayment();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(config.connectTimeout());
        requestFactory.setReadTimeout(config.readTimeout());
        return builder
                .baseUrl(config.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    public CreditCardPaymentClient creditCardPaymentClient(RestClient creditCardPaymentRestClient) {
        return new CreditCardPaymentClient(creditCardPaymentRestClient);
    }
}

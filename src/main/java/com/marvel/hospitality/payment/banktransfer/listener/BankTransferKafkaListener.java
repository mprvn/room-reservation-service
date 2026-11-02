package com.marvel.hospitality.payment.banktransfer.listener;

import com.marvel.hospitality.payment.banktransfer.exception.InvalidPaymentEventException;
import com.marvel.hospitality.payment.banktransfer.model.BankTransferPaymentEvent;
import com.marvel.hospitality.payment.banktransfer.service.BankTransferPaymentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
@Slf4j
public class BankTransferKafkaListener{

    private final JsonMapper jsonMapper;
    private final BankTransferPaymentService paymentService;

    public BankTransferKafkaListener(JsonMapper jsonMapper, BankTransferPaymentService paymentService) {
        this.jsonMapper = jsonMapper;
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "${app.kafka.bank-transfer-payment-topic}")
    public void onPaymentUpdate(ConsumerRecord<String, String> record) {
        log.debug("Received bank transfer payment update partition={} offset={}", record.partition(), record.offset());
        paymentService.processPayment(toEvent(record.value()));
    }

    private BankTransferPaymentEvent toEvent(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new InvalidPaymentEventException("Empty bank transfer payment event");
        }
        try {
            return jsonMapper.readValue(payload, BankTransferPaymentEvent.class);
        } catch (JacksonException e) {
            throw new InvalidPaymentEventException("Malformed bank transfer payment event", e);
        }
    }
}

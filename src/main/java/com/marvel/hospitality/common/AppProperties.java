package com.marvel.hospitality.common;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;


@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @Valid @NotNull CreditCardPayment creditCardPayment,
        @Valid @NotNull Reservation reservation,
        @Valid @NotNull Kafka kafka) {

    public record CreditCardPayment(
            @NotBlank String baseUrl,
            @NotNull Duration connectTimeout,
            @NotNull Duration readTimeout) {
    }

    public record Reservation(
            @Min(0) int bankTransferPaymentDeadlineDays,
            @NotBlank String cancellationCron,
            @NotEmpty Map<com.marvel.hospitality.reservation.model.RoomSegment, BigDecimal> nightlyRates) {
    }

    public record Kafka(
            @NotBlank String bankTransferPaymentTopic,
            @NotBlank String deadLetterTopic,
            @Min(0) long retryAttempts,
            @NotNull Duration retryInterval) {
    }
}

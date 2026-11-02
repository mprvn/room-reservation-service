package com.marvel.hospitality.reservation.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ConfirmationRequest (
        @NotBlank @Size(max = 200) String customerName,
        @NotBlank @Size(max = 20) String roomNumber,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull RoomSegment roomSegment,
        @NotNull PaymentMode paymentMode,
        @Size(max = 100) String paymentReference
) { }

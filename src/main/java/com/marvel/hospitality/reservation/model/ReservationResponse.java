package com.marvel.hospitality.reservation.model;

import java.math.BigDecimal;

public record ReservationResponse(
        String reservationId,
        ReservationStatus reservationStatus,
        BigDecimal totalAmount,
        BigDecimal amountPaid) {
}

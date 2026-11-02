package com.marvel.hospitality.reservation.service;

import com.marvel.hospitality.common.AppProperties;
import com.marvel.hospitality.openapi.creditcard.model.PaymentStatusResponse;
import com.marvel.hospitality.payment.creditcard.model.CreditCardPaymentStatus;
import com.marvel.hospitality.payment.creditcard.service.CreditCardPaymentClient;
import com.marvel.hospitality.reservation.model.ConfirmationRequest;
import com.marvel.hospitality.reservation.model.PaymentMode;
import com.marvel.hospitality.reservation.model.RoomSegment;
import com.marvel.hospitality.reservation.model.Reservation;
import com.marvel.hospitality.reservation.repository.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.marvel.hospitality.reservation.service.ReservationExceptions.InvalidReservationException;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class ReservationService {
    /** A room cannot be reserved for more than 30 days (nights). */
    public static final int MAX_STAY_DAYS = 30;
    private final CreditCardPaymentClient creditCardPaymentClient;
    private ReservationRepository reservationRepository;
    private ReservationIdGenerator idGenerator;
    private final Map<RoomSegment, BigDecimal> nightlyRates;
    private final int bankTransferDeadlineDays;

    public ReservationService(ReservationRepository reservationRepository, ReservationIdGenerator idGenerator, AppProperties properties, CreditCardPaymentClient creditCardPaymentClient) {
        this.idGenerator = idGenerator;
        this.reservationRepository = reservationRepository;
        this.bankTransferDeadlineDays = properties.reservation().bankTransferPaymentDeadlineDays();
        this.nightlyRates =  Map.copyOf(properties.reservation().nightlyRates());
        this.creditCardPaymentClient = creditCardPaymentClient;
    }

    public Reservation confirmReservation(ConfirmationRequest request){

        validateBusinessRules(request);
        Reservation reservation = Reservation.create(
                idGenerator.nextId(),
                request.customerName(),
                request.roomNumber(),
                request.startDate(),
                request.endDate(),
                request.roomSegment(),
                request.paymentMode(),
                request.paymentReference(),
                calculateTotalAmount(request.roomSegment(), request.startDate(),request.endDate())
                );

        switch (request.paymentMode()) {
            case CASH -> {
                reservation.confirm();
                reservation.setAmountPaid(reservation.getTotalAmount());
            }
            case CREDIT_CARD -> {
                verifyCreditCardPayment(reservation.getPaymentReference());
                reservation.setAmountPaid(reservation.getTotalAmount());
                reservation.confirm();
            }
            case BANK_TRANSFER -> {
                // stays PENDING_PAYMENT; confirmed by the bank-transfer-payment-update consumer
            }
        }
        Reservation savedReservation =   reservationRepository.save(reservation);
        log.info("Reservation {} for room {} ({} - {}) created with status {} via {}",
                savedReservation.getId(), savedReservation.getRoomNumber(), savedReservation.getStartDate(), savedReservation.getEndDate(),
                savedReservation.getStatus(), savedReservation.getPaymentMode());
        return savedReservation;
    }

    private void validateBusinessRules(ConfirmationRequest request) {
        LocalDate today = LocalDate.now();
        if (request.startDate().isBefore(today)) {
            throw new ReservationExceptions.InvalidReservationException("Reservation start date cannot be in the past");
        }
        if (!request.endDate().isAfter(request.startDate())) {
            throw new InvalidReservationException("Reservation end date must be after the start date");
        }
        long days = ChronoUnit.DAYS.between(request.startDate(), request.endDate());
        if (days > MAX_STAY_DAYS) {
            throw new InvalidReservationException(
                    "A room cannot be reserved for more than %d days".formatted(MAX_STAY_DAYS));
        }
        if (request.paymentMode() == PaymentMode.CREDIT_CARD && isBlank(request.paymentReference())) {
            throw new InvalidReservationException("Payment reference is required for credit card payments");
        }
        if (request.paymentMode() == PaymentMode.BANK_TRANSFER
                && request.startDate().minusDays(bankTransferDeadlineDays).isBefore(today)) {
            throw new InvalidReservationException(
                    "Bank transfer is only accepted for reservations starting at least %d days from today"
                            .formatted(bankTransferDeadlineDays));
        }
    }

    public Reservation getReservation(String reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationExceptions.ReservationNotFoundException(reservationId));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private void verifyCreditCardPayment(String paymentReference) {
        PaymentStatusResponse response = creditCardPaymentClient.retrievePaymentStatus(paymentReference);
        if (!Objects.equals(response.getStatus(), CreditCardPaymentStatus.CONFIRMED.name())) {
            throw new ReservationExceptions.CreditCardPaymentNotConfirmedException(paymentReference, CreditCardPaymentStatus.valueOf(response.getStatus()));
        }
    }
    public BigDecimal calculateTotalAmount(RoomSegment segment, LocalDate startDate, LocalDate endDate) {
        BigDecimal rate = this.nightlyRates.get(segment);
        if (rate == null) {
            throw new IllegalStateException("No nightly rate configured for room segment " + segment);
        }
        long nights = ChronoUnit.DAYS.between(startDate, endDate);
        return rate.multiply(BigDecimal.valueOf(nights)).setScale(2, RoundingMode.HALF_UP);
    }

}

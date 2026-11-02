package com.marvel.hospitality.reservation.service;


import com.marvel.hospitality.common.AppProperties;
import com.marvel.hospitality.reservation.model.PaymentMode;
import com.marvel.hospitality.reservation.model.Reservation;
import com.marvel.hospitality.reservation.model.ReservationStatus;
import com.marvel.hospitality.reservation.repository.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class CancellationService {

    private final ReservationRepository reservationRepository;
    private final int deadlineDays;

    public CancellationService(ReservationRepository reservationRepository, AppProperties appProperties) {
        this.reservationRepository = reservationRepository;
        this.deadlineDays = appProperties.reservation().bankTransferPaymentDeadlineDays();
    }

    /**
     * List the Reservations that are  ReservationStatus.PENDING_PAYMENT even after deadline and calls the
     * cancellation service
     * @return
     */
    public int cancelUnpaidBankTransferReservations() {
        LocalDate cutoff = LocalDate.now().plusDays(deadlineDays);
        List<Reservation> overdue = reservationRepository.findByPaymentModeAndStatusAndStartDateBefore(
                PaymentMode.BANK_TRANSFER, ReservationStatus.PENDING_PAYMENT, cutoff);

        int cancelled = 0;
        for (Reservation reservation : overdue) {
            if (cancelInOwnTransaction(reservation.getId())) {
                cancelled++;
            }
        }
        if (!overdue.isEmpty()) {
            log.info("Cancelled {} of {} unpaid bank transfer reservations starting before {}",
                    cancelled, overdue.size(), cutoff);
        }
        return cancelled;
    }

    /**
     * Each transaction status is set to canceled
     * @param reservationId
     * @return
     */
    private boolean cancelInOwnTransaction(String reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        // re-check: a payment may have confirmed the reservation since the query ran
        if (reservation == null || reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            return false;
        }
        reservation.cancel();
        reservationRepository.save(reservation);
        log.info("Reservation {} cancelled: bank transfer not received in time (paid {} of {})",
                reservation.getId(), reservation.getAmountPaid(), reservation.getTotalAmount());
        return true;
    }
}

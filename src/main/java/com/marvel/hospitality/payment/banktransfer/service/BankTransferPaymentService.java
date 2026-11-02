package com.marvel.hospitality.payment.banktransfer.service;

import com.marvel.hospitality.payment.banktransfer.exception.InvalidPaymentEventException;
import com.marvel.hospitality.payment.banktransfer.exception.UnknowReservationException;
import com.marvel.hospitality.payment.banktransfer.model.BankTransferPayment;
import com.marvel.hospitality.payment.banktransfer.model.BankTransferPaymentEvent;
import com.marvel.hospitality.payment.banktransfer.repository.BankTransferPaymentRepository;
import com.marvel.hospitality.reservation.model.PaymentMode;
import com.marvel.hospitality.reservation.model.Reservation;
import com.marvel.hospitality.reservation.model.ReservationStatus;
import com.marvel.hospitality.reservation.repository.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Slf4j
@Service
public class BankTransferPaymentService {

    private final ReservationRepository reservationRepository;
    private final BankTransferPaymentRepository BankTransferpaymentRepository;
    private static final Pattern FORMAT = Pattern.compile("^\\s*(\\S{10})\\s+(\\S{8})\\s*$");

    public BankTransferPaymentService(ReservationRepository reservationRepository, BankTransferPaymentRepository BankTransferpaymentRepository) {
        this.reservationRepository = reservationRepository;
        this.BankTransferpaymentRepository = BankTransferpaymentRepository;
    }

    @Transactional
    public void processPayment(BankTransferPaymentEvent event) {

       if(event.transactionDescription() == null){
            throw new InvalidPaymentEventException("Transaction description is missing");
       }
        Matcher matcher = FORMAT.matcher(event.transactionDescription());
        if(matcher.matches()) {
            String reservationId = matcher.group(2).toUpperCase();
            String eToEUniqueId = matcher.group(1);
         Reservation reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() ->new UnknowReservationException(reservationId, event.paymentId()));

            BankTransferpaymentRepository.save(
                    new BankTransferPayment(
                            event.paymentId(),
                            reservation.getId(),
                            eToEUniqueId,
                            event.debtorAccountNumber(),
                            event.amountReceived(),
                            event.transactionDescription(),
                            Instant.now()
                            )
            );

            if(reservation.getPaymentMode() != PaymentMode.BANK_TRANSFER){
                log.warn("Bank transfer {} received for reservation {} with payment mode {}; recorded for reconciliation",
                        event.paymentId(), reservation.getId(), reservation.getPaymentMode());
                return;
            }
            reservation.registerAmount(event.amountReceived());
            if(reservation.getStatus() == ReservationStatus.PENDING_PAYMENT ) {
                reservation.confirm();
            } else {
              log.info ("Payment with id {} andd reservation id {} is cancelled a refund of {} amount will be issued",
                      event.paymentId(), reservation.getId(), event.amountReceived());
            }
         reservationRepository.save(reservation);
        }

    }
}

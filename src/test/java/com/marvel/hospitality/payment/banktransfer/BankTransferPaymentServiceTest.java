package com.marvel.hospitality.payment.banktransfer;

import com.marvel.hospitality.payment.banktransfer.model.BankTransferPayment;
import com.marvel.hospitality.payment.banktransfer.model.BankTransferPaymentEvent;
import com.marvel.hospitality.payment.banktransfer.repository.BankTransferPaymentRepository;
import com.marvel.hospitality.payment.banktransfer.service.BankTransferPaymentService;
import com.marvel.hospitality.reservation.model.PaymentMode;
import com.marvel.hospitality.reservation.model.Reservation;
import com.marvel.hospitality.reservation.model.ReservationStatus;
import com.marvel.hospitality.reservation.model.RoomSegment;
import com.marvel.hospitality.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource( locations = "classpath:application-test.yml")
public class BankTransferPaymentServiceTest {

    @MockitoBean private  ReservationRepository reservationRepository;
    @MockitoBean private  BankTransferPaymentRepository BankTransferpaymentRepository;
    private BankTransferPaymentService service;
    private static final String RESERVATION_ID = "P4145478";
    private Reservation reservation;
    @BeforeEach
    void setUp() {
        service = new BankTransferPaymentService(reservationRepository, BankTransferpaymentRepository);
        LocalDate today  = LocalDate.now();
        reservation = createReservationRequest(RESERVATION_ID, today.plusDays(10), today.plusDays(12), "240.00");
    }

    @Test
    void fullPaymentConfirmsReservation() {
        givenReservationExists();

        service.processPayment(event("PAY-1", "240.00"));

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        ArgumentCaptor<BankTransferPayment> captor = ArgumentCaptor.forClass(BankTransferPayment.class);
        verify(BankTransferpaymentRepository).save(captor.capture());
        assertThat(captor.getValue().getEtoEUniqueId()).isEqualTo("1401541457");
        assertThat(captor.getValue().getReservationId()).isEqualTo(RESERVATION_ID);
        verify(reservationRepository).save(reservation);
    }

    private void givenReservationExists() {
        when(BankTransferpaymentRepository.existsById(any())).thenReturn(false);
        when(reservationRepository.findById(RESERVATION_ID)).thenReturn(Optional.of(reservation));
    }

    private static BankTransferPaymentEvent event(String paymentId, String amount) {
        return new BankTransferPaymentEvent(paymentId, "NL01BANK0123456789", new BigDecimal(amount),
                "1401541457 " + RESERVATION_ID);
    }

    private static Reservation createReservationRequest(String id, LocalDate start, LocalDate end, String total) {
        return   Reservation.create(id, "Tony Stark", "101", start, end, RoomSegment.MEDIUM,
                PaymentMode.BANK_TRANSFER, null, new BigDecimal(total));
    }

}

package com.marvel.hospitality.reservation.service;

import com.marvel.hospitality.common.AppProperties;
import com.marvel.hospitality.reservation.model.*;
import com.marvel.hospitality.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource( locations = "classpath:application-test.yml")
public class CancellationServiceTest {
    @MockitoBean
    private  ReservationRepository reservationRepository;
    @Autowired  AppProperties properties;
    private CancellationService service;

    @BeforeEach
    void setUp() {
        service = new CancellationService(reservationRepository,  properties);
    }
    @Test
    void queriesWithCutoffOfTodayPlusDeadlineDays() {
        when(reservationRepository.findByPaymentModeAndStatusAndStartDateBefore(
                PaymentMode.BANK_TRANSFER, ReservationStatus.PENDING_PAYMENT, LocalDate.now().plusDays(2)))
                .thenReturn(List.of());

        assertThat(service.cancelUnpaidBankTransferReservations()).isZero();
    }

    @Test
    void cancelsOverduePendingReservation() {
        LocalDate today =  LocalDate.now();
        Reservation overdue = createReservationRequest("OVERDUE1", today.plusDays(1), today.plusDays(3), "240.00");
        when(reservationRepository.findByPaymentModeAndStatusAndStartDateBefore(any(), any(), any()))
                .thenReturn(List.of(overdue));
        when(reservationRepository.findById("OVERDUE1")).thenReturn(Optional.of(overdue));

        int cancelled = service.cancelUnpaidBankTransferReservations();

        assertThat(cancelled).isEqualTo(1);
        assertThat(overdue.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        verify(reservationRepository).save(overdue);
    }

    private static Reservation createReservationRequest(String id, LocalDate start, LocalDate end, String total) {
        return   Reservation.create(id, "Tony Stark", "101", start, end, RoomSegment.MEDIUM,
                PaymentMode.BANK_TRANSFER, null, new BigDecimal(total));
    }

}

package com.marvel.hospitality.reservation.service;

import com.marvel.hospitality.common.AppProperties;
import com.marvel.hospitality.openapi.creditcard.model.PaymentStatusResponse;
import com.marvel.hospitality.payment.creditcard.service.CreditCardPaymentClient;
import com.marvel.hospitality.reservation.model.*;
import com.marvel.hospitality.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@SpringBootTest
@TestPropertySource ( locations = "classpath:application-test.yml")
public class ReservationServiceTest {

    @MockitoBean
    private ReservationRepository reservationRepository;
    @MockitoBean
    private CreditCardPaymentClient creditCardPaymentClient;
    @MockitoBean
    private ReservationIdGenerator idGenerator;
    @Autowired
    AppProperties properties;

    private ReservationService service;

    @BeforeEach
    void setUp() {
        service = new ReservationService(reservationRepository,idGenerator, properties, creditCardPaymentClient);
    }

    @Test
    void cashReservationIsConfirmedImmediately() {
        // paymentreference is null
        when(idGenerator.nextId()).thenReturn("ABCD2345");
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));
       LocalDate today = LocalDate.now();
        Reservation reservation = service.confirmReservation(createReservationRequest(PaymentMode.CASH, null, today , today.plusDays(3)));

        assertThat(reservation.getId()).isEqualTo("ABCD2345");
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(reservation.getTotalAmount()).isEqualByComparingTo("150.00"); // 3 nights x MEDIUM 120
        verifyNoInteractions(creditCardPaymentClient);
    }

    private static ConfirmationRequest createReservationRequest(PaymentMode paymentMode,String reference, LocalDate startdate, LocalDate endDate){
        return new ConfirmationRequest("Sania mirza", "111", startdate, endDate, RoomSegment.MEDIUM, paymentMode, reference  );
    }

    @Test
    void creditCardReservationIsConfirmedWhenPaymentConfirmed() {
        when(idGenerator.nextId()).thenReturn("ABCD2345");
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        when(creditCardPaymentClient.retrievePaymentStatus("CC123"))
                .thenReturn( PaymentStatusResponse.builder().lastUpdateDate(OffsetDateTime.now().toString()).status("CONFIRMED").build());
        LocalDate today = LocalDate.now();
        Reservation reservation = service.confirmReservation(
                createReservationRequest(PaymentMode.CREDIT_CARD, "CC123", today.plusDays(1), today.plusDays(2)));

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void creditCardReservationFailsWhenPaymentRejected() {
        when(idGenerator.nextId()).thenReturn("ABCD2345");
        when(creditCardPaymentClient.retrievePaymentStatus("CC123"))
                .thenReturn(PaymentStatusResponse.builder().lastUpdateDate(OffsetDateTime.now().toString()).status("REJECTED").build());
        LocalDate today = LocalDate.now();
        assertThatThrownBy(() -> service.confirmReservation(
                createReservationRequest(PaymentMode.CREDIT_CARD, "CC123", today.plusDays(1), today.plusDays(2))))
                .isInstanceOf(ReservationExceptions.CreditCardPaymentNotConfirmedException.class);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void creditCardRequiresPaymentReference() {
        LocalDate today = LocalDate.now();
        assertThatThrownBy(() -> service.confirmReservation(
                createReservationRequest(PaymentMode.CREDIT_CARD, " ", today.plusDays(1), today.plusDays(2))))
                .isInstanceOf(ReservationExceptions.InvalidReservationException.class)
                .hasMessageContaining("Payment reference");
    }

    @Test
    void stayOfExactly30DaysIsAllowed() {
        LocalDate today = LocalDate.now();
        when(idGenerator.nextId()).thenReturn("ABCD2345");
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        Reservation reservation = service.confirmReservation(
                createReservationRequest(PaymentMode.CASH, null, today, today.plusDays(30)));

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void stayLongerThan30DaysIsRejected() {
        LocalDate today = LocalDate.now();
        assertThatThrownBy(() -> service.confirmReservation(
                createReservationRequest(PaymentMode.CASH, null, today, today.plusDays(31))))
                .isInstanceOf(ReservationExceptions.InvalidReservationException.class)
                .hasMessageContaining("30 days");
    }

    @Test
    void endDateMustBeAfterStartDate() {
        LocalDate today = LocalDate.now();
        assertThatThrownBy(() -> service.confirmReservation(createReservationRequest(PaymentMode.CASH, null, today, today)))
                .isInstanceOf(ReservationExceptions.InvalidReservationException.class);
    }
}

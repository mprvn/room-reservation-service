package com.marvel.hospitality.reservation.controller;

import com.marvel.hospitality.common.GlobalExceptionHandler;
import com.marvel.hospitality.payment.creditcard.model.CreditCardPaymentStatus;
import com.marvel.hospitality.reservation.model.PaymentMode;
import com.marvel.hospitality.reservation.model.Reservation;
import com.marvel.hospitality.reservation.model.RoomSegment;
import com.marvel.hospitality.reservation.service.ReservationExceptions;
import com.marvel.hospitality.reservation.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@TestPropertySource( locations = "classpath:application-test.yml")
public class ReservationControllerTest {

    @MockitoBean
    private ReservationService reservationService;
    private MockMvc mockMvc;

    private static final String MOCK_RESPONSE_CONFIRMED = """
            {
              "customerName": "Tony Stark",
              "roomNumber": "101",
              "startDate": "2030-01-10",
              "endDate": "2030-01-12",
              "roomSegment": "EXTRA_LARGE",
              "paymentMode": "CASH"
            }
            """;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new ReservationController(reservationService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void confirmsReservation(@Autowired  MockMvc mockMvc) throws Exception {
        Reservation reservation = Reservation.create("ABCD2345", "Tony Stark", "101",
                LocalDate.of(2030, 1, 10), LocalDate.of(2030, 1, 12),
                RoomSegment.EXTRA_LARGE,
                PaymentMode.CASH, null, new BigDecimal("500.00"));
        reservation.confirm();
        when(reservationService.confirmReservation(any())).thenReturn(reservation);

        mockMvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON).content(MOCK_RESPONSE_CONFIRMED))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/reservations/ABCD2345"))
                .andExpect(jsonPath("$.reservationId").value("ABCD2345"))
                .andExpect(jsonPath("$.reservationStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalAmount").value(500.00));
    }

    @Test
    void missingFieldsReturn400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(reservationService);
    }

    @Test
    void unknownReservationReturns404() throws Exception {
        when(reservationService.getReservation("ERROR1234")).thenThrow(new ReservationExceptions.ReservationNotFoundException("ERROR1234"));
        mockMvc.perform(get("/reservations/ERROR1234"))
                .andExpect(status().isNotFound());
    }

    @Test
    void validReservation() throws Exception {
        Reservation reservation = Reservation.create("ABCD2345", "Tony Stark", "101",
                LocalDate.of(2030, 1, 10), LocalDate.of(2030, 1, 12),
                RoomSegment.EXTRA_LARGE,
                PaymentMode.CASH, null, new BigDecimal("500.00"));
        when(reservationService.getReservation("VALID1234")).thenReturn(reservation);
        mockMvc.perform(get("/reservations/VALID1234"))
                .andExpect(status().is2xxSuccessful());
    }
    @Test
    void rejectedCreditCardReturns422() throws Exception {
        when(reservationService.confirmReservation(any()))
                .thenThrow(new ReservationExceptions.CreditCardPaymentNotConfirmedException("CC1", CreditCardPaymentStatus.REJECTED));

        mockMvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON).content(MOCK_RESPONSE_CONFIRMED))
                .andExpect(status().is(422));
    }
}

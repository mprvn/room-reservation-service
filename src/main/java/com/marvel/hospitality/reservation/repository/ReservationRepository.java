package com.marvel.hospitality.reservation.repository;

import com.marvel.hospitality.reservation.model.PaymentMode;
import com.marvel.hospitality.reservation.model.Reservation;
import com.marvel.hospitality.reservation.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, String> {


    List<Reservation> findByPaymentModeAndStatusAndStartDateBefore(PaymentMode paymentMode,
                                                                   ReservationStatus status,
                                                                   LocalDate startDate);
}

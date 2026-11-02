package com.marvel.hospitality.reservation.controller;


import com.marvel.hospitality.reservation.model.ConfirmationRequest;
import com.marvel.hospitality.reservation.model.Reservation;
import com.marvel.hospitality.reservation.model.ReservationResponse;
import com.marvel.hospitality.reservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> confirmReservation(@Valid @RequestBody ConfirmationRequest request) {
        Reservation reservation = reservationService.confirmReservation(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(reservation.getId())
                .toUri();
        return ResponseEntity.created(location).body(mapResponse(reservation));
    }

    private ReservationResponse mapResponse(Reservation reservation) {
        return new ReservationResponse(reservation.getId(), reservation.getStatus(),
                reservation.getTotalAmount(), reservation.getAmountPaid());
    }

    @GetMapping("/{reservationId}")
    public ReservationResponse getReservation(@PathVariable String reservationId) {
        return mapResponse(reservationService.getReservation(reservationId));
    }
}

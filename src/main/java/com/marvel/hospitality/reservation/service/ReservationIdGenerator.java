package com.marvel.hospitality.reservation.service;

import com.marvel.hospitality.reservation.repository.ReservationRepository;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Generates 8 character reservation ids.
 * transaction description format: {@code <E2E id (10)> <reservationId (8)>}.
 */
@Component
public class ReservationIdGenerator {

    static final int LENGTH = 8;
    private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int MAX_ATTEMPTS = 10;

    private final SecureRandom random = new SecureRandom();
    private final ReservationRepository reservationRepository;

    public ReservationIdGenerator(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public String nextId() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String candidate = randomId();
            if (!reservationRepository.existsById(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate a unique reservation id");
    }

    private String randomId() {
        char[] chars = new char[LENGTH];
        for (int i = 0; i < LENGTH; i++) {
            chars[i] = ALPHABET[random.nextInt(ALPHABET.length)];
        }
        return new String(chars);
    }
}

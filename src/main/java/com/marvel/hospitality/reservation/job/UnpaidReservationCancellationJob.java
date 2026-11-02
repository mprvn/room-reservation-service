package com.marvel.hospitality.reservation.job;

import com.marvel.hospitality.reservation.service.CancellationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UnpaidReservationCancellationJob {

    private final CancellationService cancellationService;

    public UnpaidReservationCancellationJob(CancellationService cancellationService) {
        this.cancellationService = cancellationService;
    }

    @Scheduled(cron = "${app.reservation.cancellation-cron}")
    public void cancelUnpaidReservations() {
        log.debug("Running unpaid bank transfer reservation cancellation job");
        try {
            cancellationService.cancelUnpaidBankTransferReservations();
        } catch (RuntimeException e) {
            log.error("Unpaid reservation cancellation job failed", e);
        }
    }
}

package com.stays.reservation.adapter.in.scheduling;

import com.stays.reservation.application.command.ReservationProgressCommands;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationProgressAbandonmentScheduler {
    private final ReservationProgressCommands progress;

    public ReservationProgressAbandonmentScheduler(ReservationProgressCommands progress) {
        this.progress = progress;
    }

    @Scheduled(fixedDelayString = "${app.reservation-progress.sweep-interval-ms:60000}")
    public void abandonInactiveSessions() {
        progress.abandonInactiveSessions();
    }
}

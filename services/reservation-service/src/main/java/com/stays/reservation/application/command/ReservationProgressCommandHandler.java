package com.stays.reservation.application.command;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import com.stays.reservation.application.port.ReservationProgressStore;
import com.stays.reservation.domain.ReservationProgressScreen;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ReservationProgressCommandHandler implements ReservationProgressCommands {
    private final ReservationProgressStore progress;
    private final Clock clock;
    private final Duration inactivityTimeout;

    public ReservationProgressCommandHandler(
            ReservationProgressStore progress,
            Clock clock,
            @Value("${app.reservation-progress.inactivity-timeout:PT30M}") Duration inactivityTimeout) {
        this.progress = progress;
        this.clock = clock;
        this.inactivityTimeout = inactivityTimeout;
    }

    @Override
    public void recordScreen(UUID sessionId, ReservationProgressScreen screen) {
        Instant now = Instant.now(clock);
        if (screen == ReservationProgressScreen.CONFIRMATION) {
            progress.complete(sessionId, now);
            return;
        }
        progress.recordScreen(sessionId, screen, now);
    }

    @Override
    public void abandonInactiveSessions() {
        Instant now = Instant.now(clock);
        progress.abandonInactive(now.minus(inactivityTimeout), now);
    }
}

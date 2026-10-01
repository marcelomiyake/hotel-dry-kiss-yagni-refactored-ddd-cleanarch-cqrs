package com.stays.reservation.application.port;

import java.time.Instant;
import java.util.UUID;

import com.stays.reservation.domain.ReservationProgressScreen;

public interface ReservationProgressStore {
    void recordScreen(UUID sessionId, ReservationProgressScreen screen, Instant recordedAt);

    void complete(UUID sessionId, Instant completedAt);

    int abandonInactive(Instant lastActivityBefore, Instant abandonedAt);
}

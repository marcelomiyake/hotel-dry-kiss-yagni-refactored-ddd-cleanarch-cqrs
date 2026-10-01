package com.stays.reservation.application.command;

import java.util.UUID;

import com.stays.reservation.domain.ReservationProgressScreen;

public interface ReservationProgressCommands {
    void recordScreen(UUID sessionId, ReservationProgressScreen screen);

    void abandonInactiveSessions();
}

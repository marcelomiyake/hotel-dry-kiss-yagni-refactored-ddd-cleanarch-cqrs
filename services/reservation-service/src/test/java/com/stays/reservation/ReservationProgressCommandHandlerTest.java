package com.stays.reservation;

import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import com.stays.reservation.application.command.ReservationProgressCommandHandler;
import com.stays.reservation.application.port.ReservationProgressStore;
import com.stays.reservation.domain.ReservationProgressScreen;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationProgressCommandHandlerTest {
    private static final Instant NOW = Instant.parse("2026-10-01T12:00:00Z");

    @Mock
    private ReservationProgressStore progress;

    @Test
    void recordsScreensCompletesSuccessfulJourneysAndUsesTheConfiguredInactivityWindow() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        ReservationProgressCommandHandler handler = new ReservationProgressCommandHandler(
                progress, clock, Duration.ofMinutes(30));
        UUID sessionId = UUID.randomUUID();

        handler.recordScreen(sessionId, ReservationProgressScreen.DETAILS);
        handler.recordScreen(sessionId, ReservationProgressScreen.CHECKOUT);
        handler.recordScreen(sessionId, ReservationProgressScreen.CONFIRMATION);
        handler.abandonInactiveSessions();

        verify(progress).recordScreen(sessionId, ReservationProgressScreen.DETAILS, NOW);
        verify(progress).recordScreen(sessionId, ReservationProgressScreen.CHECKOUT, NOW);
        verify(progress).complete(sessionId, NOW);
        verify(progress).abandonInactive(NOW.minus(Duration.ofMinutes(30)), NOW);
    }
}

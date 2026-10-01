package com.stays.reservation;

import static org.mockito.Mockito.verify;

import com.stays.reservation.adapter.in.scheduling.ReservationProgressAbandonmentScheduler;
import com.stays.reservation.application.command.ReservationProgressCommands;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationProgressAbandonmentSchedulerTest {
    @Mock
    private ReservationProgressCommands progress;

    @InjectMocks
    private ReservationProgressAbandonmentScheduler scheduler;

    @Test
    void asksTheCommandBoundaryToExpireInactiveSessions() {
        scheduler.abandonInactiveSessions();

        verify(progress).abandonInactiveSessions();
    }
}

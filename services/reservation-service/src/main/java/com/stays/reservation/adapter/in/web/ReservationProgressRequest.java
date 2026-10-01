package com.stays.reservation.adapter.in.web;

import java.util.UUID;

import com.stays.reservation.domain.ReservationProgressScreen;
import jakarta.validation.constraints.NotNull;

public record ReservationProgressRequest(
        @NotNull UUID sessionId,
        @NotNull ReservationProgressScreen screen) {
}

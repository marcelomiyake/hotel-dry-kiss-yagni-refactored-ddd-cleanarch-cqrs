package com.stays.reservation.adapter.out.jdbc;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.stays.reservation.application.port.ReservationProgressStore;
import com.stays.reservation.domain.ReservationProgressScreen;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ReservationProgressRepository implements ReservationProgressStore {
    private static final String INSERT_EVENT = "INSERT INTO reservations.reservation_funnel_events "
            + "(session_id, event_type, screen, recorded_at) VALUES (?, ?, ?, ?)";
    private final JdbcTemplate jdbc;

    public ReservationProgressRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void recordScreen(UUID sessionId, ReservationProgressScreen screen, Instant recordedAt) {
        int inserted = jdbc.update(
                "INSERT INTO reservations.reservation_funnel_sessions "
                        + "(session_id, status, last_screen, started_at, last_activity_at) "
                        + "VALUES (?, 'IN_PROGRESS', ?, ?, ?) ON CONFLICT (session_id) DO NOTHING",
                sessionId, screen.name(), Timestamp.from(recordedAt), Timestamp.from(recordedAt));

        if (inserted == 1) {
            insertEvent(sessionId, "STARTED", screen.name(), recordedAt);
            return;
        }

        int updated = jdbc.update(
                "UPDATE reservations.reservation_funnel_sessions SET last_screen = ?, last_activity_at = ? "
                        + "WHERE session_id = ? AND status = 'IN_PROGRESS'",
                screen.name(), Timestamp.from(recordedAt), sessionId);
        if (updated == 1) {
            insertEvent(sessionId, "SCREEN_VIEWED", screen.name(), recordedAt);
        }
    }

    @Override
    @Transactional
    public void complete(UUID sessionId, Instant completedAt) {
        int updated = jdbc.update(
                "UPDATE reservations.reservation_funnel_sessions SET status = 'COMPLETED', "
                        + "last_screen = 'CONFIRMATION', last_activity_at = ?, completed_at = ? "
                        + "WHERE session_id = ? AND status = 'IN_PROGRESS'",
                Timestamp.from(completedAt), Timestamp.from(completedAt), sessionId);
        if (updated == 1) {
            insertEvent(sessionId, "COMPLETED", ReservationProgressScreen.CONFIRMATION.name(), completedAt);
        }
    }

    @Override
    @Transactional
    public int abandonInactive(Instant lastActivityBefore, Instant abandonedAt) {
        List<AbandonedSession> sessions = jdbc.query(
                "UPDATE reservations.reservation_funnel_sessions SET status = 'ABANDONED', abandoned_at = ? "
                        + "WHERE status = 'IN_PROGRESS' AND last_activity_at < ? "
                        + "RETURNING session_id, last_screen",
                (result, row) -> new AbandonedSession(
                        result.getObject("session_id", UUID.class), result.getString("last_screen")),
                Timestamp.from(abandonedAt), Timestamp.from(lastActivityBefore));
        sessions.forEach(session -> insertEvent(session.sessionId(), "ABANDONED", session.lastScreen(), abandonedAt));
        return sessions.size();
    }

    private void insertEvent(UUID sessionId, String eventType, String screen, Instant recordedAt) {
        jdbc.update(INSERT_EVENT, sessionId, eventType, screen, Timestamp.from(recordedAt));
    }

    private record AbandonedSession(UUID sessionId, String lastScreen) {
    }
}

package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.Instant;

@Data
public class Feed {
    private Long userId;
    private Long entityId;
    private Long eventId;
    private Long operationId;
    private Long timestamp;

    public Feed(Long userId, Long entityId, Long eventId, Long operationId) {
        this.userId = userId;
        this.entityId = entityId;
        this.eventId = eventId;
        this.operationId = operationId;
        this.timestamp = Instant.now().toEpochMilli();
    }
}

package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
public class Feed {
    private Long eventId;
    private Long userId;
    private Long entityId;
    private EventType eventType;
    private Operation operation;
    private Long timestamp;

    public Feed(Long userId, Long entityId, EventType eventType, Operation operation) {
        this.userId = userId;
        this.entityId = entityId;
        this.eventType = eventType;
        this.operation = operation;
        this.timestamp = Instant.now().toEpochMilli();
    }
}

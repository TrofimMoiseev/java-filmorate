package ru.yandex.practicum.filmorate.DTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = {"eventId"})
public class FeedDTO {
        private Long eventId;
        private Long userId;
        private Long entityId;
        private String eventType;
        private String operation;
        private Long timestamp;
}

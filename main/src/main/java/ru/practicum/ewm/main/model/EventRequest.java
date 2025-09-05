package ru.practicum.ewm.main.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime created;
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    Event event;
    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    User requester;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    RequestStatus status;
}

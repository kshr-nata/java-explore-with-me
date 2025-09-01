package ru.practicum.ewm.main.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private String annotation;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;
    private String description;
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;
    @ManyToOne
    @JoinColumn(name = "initiator_id")
    private User initiator;
    @NotNull
    private Float lat;
    @NotNull
    private Float lon;
    @NotNull
    private Boolean paid;
    @Column(name = "participant_limit", nullable = false)
    private Long participantLimit;
    @Column(name = "published_on", nullable = false)
    private LocalDateTime publishedOn;
    @Column(name = "request_moderation", nullable = false)
    private Boolean requestModeration;
    @Enumerated(EnumType.STRING)
    private EventState state;
    @NotNull
    private String title;
    @Transient
    private Long views;
    @Transient
    private Long confirmedRequests;
}

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
    private Integer id;
    @NotNull
    private String annotation;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    private LocalDateTime createdOn;
    private String description;
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
    private Integer participantLimit;
    private LocalDateTime publishedOn;
    private Boolean requestModeration;
    @Enumerated(EnumType.STRING)
    private EventState state;
    @NotNull
    private String title;
    @Transient
    private Integer views;
    @Transient
    private Integer confirmedRequests;
}

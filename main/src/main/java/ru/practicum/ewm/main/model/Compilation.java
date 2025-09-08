package ru.practicum.ewm.main.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Table(name = "compilations")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private Boolean pinned;
    @ManyToMany(fetch = FetchType.LAZY)
    @Builder.Default
    @JoinTable(name = "compilation_events", joinColumns = @JoinColumn(name = "compilation_id"), inverseJoinColumns = @JoinColumn(name = "event_id"))
    private List<Event> events = new ArrayList<>();
}

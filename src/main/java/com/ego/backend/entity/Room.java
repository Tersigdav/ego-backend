package com.ego.backend.entity;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "rooms")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder

public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "img_url")
    private String imgUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    private boolean explorable;

    @ElementCollection
    @CollectionTable(name = "room_exits", joinColumns = @JoinColumn(name = "room_id"))
    @MapKeyColumn(name = "direction")
    @Column(name = "target_room_id")
    private Map<String, Long> exits = new HashMap<>();

}

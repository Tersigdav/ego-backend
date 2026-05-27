package com.ego.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "is_pickable")
    private boolean pickable;

    @Column(name = "is_consumable")
    private boolean consumable;

    private String effect;

    @Column(name = "interaction_state")
    private int interactionState = 0;
}


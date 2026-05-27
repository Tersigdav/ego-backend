package com.ego.backend.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class Enemy extends GameCharacter {
    private int attackDamage;
    private String lootId;
}

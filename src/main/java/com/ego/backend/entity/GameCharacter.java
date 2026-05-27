package com.ego.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public abstract class GameCharacter {
    private String id;
    private String name;
    private int hp;
    private int maxHp;
    private List<Item> inventory = new ArrayList<>();

}

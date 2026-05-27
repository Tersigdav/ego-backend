package com.ego.backend.entity;



import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class NonPlayableCharacter extends GameCharacter {
    private String dialogue;
    private boolean friendly;
}

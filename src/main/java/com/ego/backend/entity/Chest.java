package com.ego.backend.entity;

import java.util.ArrayList;
import java.util.List;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class Chest extends Item {
    private List<Item> content = new ArrayList<>();
    private boolean locked;
    private String keyId;
}

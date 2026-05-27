package com.ego.backend.repository;

import com.ego.backend.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByNameIgnoreCaseAndRoomId(String name, Long roomId);
    List<Item> findAllByNameIgnoreCase(String name);
}

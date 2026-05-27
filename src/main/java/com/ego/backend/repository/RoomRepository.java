package com.ego.backend.repository;

import com.ego.backend.database.DatabaseInterface;
import com.ego.backend.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
}

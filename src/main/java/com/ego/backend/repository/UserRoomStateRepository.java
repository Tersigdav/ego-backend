package com.ego.backend.repository;

import com.ego.backend.entity.UserRoomState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRoomStateRepository
        extends JpaRepository<UserRoomState, Long> {

    Optional<UserRoomState>
    findByUserIdAndItemId(Long userId, Long itemId);
    Optional<UserRoomState> findByUserIdAndRoomId(Long userId, Long roomId);
}
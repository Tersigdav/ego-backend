package com.ego.backend.repository;

import com.ego.backend.entity.UserItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserItemRepository extends JpaRepository<UserItem, Long> {

    List<UserItem> findByUserId(Long userId);

    boolean existsByUserIdAndItemId(Long userId, Long itemId);

    Optional<UserItem> findByUserIdAndItemId(
            Long userId,
            Long itemId
    );
}

package com.soi.backend.friend.repository;

import com.soi.backend.friend.entity.Friend;
import com.soi.backend.friend.entity.FriendStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    Optional<Friend> findByRequesterIdAndReceiverId(Long requesterId, Long receiverId);
    Optional<Friend> findByRequesterIdAndReceiverIdAndStatus(Long requesterId, Long receiverId, FriendStatus status);
    List<Friend> findAllByRequesterIdAndStatus(Long requesterId, FriendStatus status);
}

package com.soi.backend.friend.repository;

import com.soi.backend.friend.entity.Friend;
import com.soi.backend.friend.entity.FriendStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    Optional<Friend> findByRequesterIdAndReceiverId(Long requesterId, Long receiverId);
    List<Friend> findAllByRequesterIdAndStatus(Long requesterId, FriendStatus status);

    @Query("""
    SELECT f FROM Friend f
    WHERE 
        ( f.requesterId = :userId AND f.requesterDeleted = false )
        OR
        ( f.receiverId = :userId AND f.receiverDeleted = false )
        AND f.status = 'ACCEPTED'
        """)
    List<Friend> findAllAcceptedFriends(@Param("userId") Long userId);

    @Query("""
    SELECT f FROM Friend f
    WHERE 
        ( (f.requesterId = :userId AND f.requesterDeleted = false) AND f.receiverId = :targetId )
        OR
        ( (f.receiverId = :userId AND f.receiverDeleted = false) AND f.requesterId = :targetId )
        AND f.status = 'ACCEPTED'
        """)
    Optional<Friend> findAcceptedFriend(@Param("userId") Long userId, @Param("targetId") Long targetId);
}

package com.soi.backend.domain.friend.repository;

import com.soi.backend.domain.friend.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    Optional<Friend> findFriendByRequesterIdAndReceiverId(Long requesterId, Long receiverId);

    @Query("""
    SELECT f FROM Friend f
    WHERE 
        (
            (f.requesterId = :requesterId AND f.receiverId = :receiverId)
            OR
            (f.requesterId = :receiverId AND f.receiverId = :requesterId)
        )
    """)
    Optional<Friend> findByRequesterIdAndReceiverId(
            @Param("requesterId") Long requesterId,
            @Param("receiverId") Long receiverId
    );

    @Query("""
    SELECT f FROM Friend f
    WHERE 
        (
            ( f.requesterId = :userId AND f.requesterDeleted = false)
            OR
            (f.receiverId = :userId AND f.receiverDeleted = false)
        )
        AND f.status = 'ACCEPTED'
        """)
    List<Friend> findAllAcceptedFriendsByUserId(@Param("userId") Long userId);

    @Query("""
    SELECT f FROM Friend f
    WHERE 
        (
            ( f.requesterId = :userId AND f.requesterDeleted = false)
            OR
            (f.receiverId = :userId AND f.receiverDeleted = false)
        )
        AND f.status = 'ACCEPTED'
        """)
    List<Friend> findAllFriendsByUserId(@Param("userId") Long userId);

    @Query("""
    SELECT f FROM Friend f
    WHERE 
        (
            (f.requesterId = :userId AND f.requesterDeleted = false) AND f.receiverId = :targetId
        )
        OR
        (
            (f.receiverId = :userId AND f.receiverDeleted = false) AND f.requesterId = :targetId
        )
    """)
    Optional<Friend> findFriend(@Param("userId") Long userId, @Param("targetId") Long targetId);

    @Query("""
        SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END
        FROM Friend f
        WHERE
            (
                (f.requesterId = :userId AND f.receiverId = :targetId AND f.requesterDeleted = false)
                OR
                (f.receiverId = :userId AND f.requesterId = :targetId AND f.receiverDeleted = false)
            )
            AND f.status = 'ACCEPTED'
    """)
    boolean isFriend(
            @Param("userId") Long userId,
            @Param("targetId") Long targetId
    );
}

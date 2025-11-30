package com.soi.backend.domain.friend.repository;

import com.soi.backend.domain.friend.entity.FriendRequestQueue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRequestQueueRepository extends JpaRepository<FriendRequestQueue, Long> {
    List<FriendRequestQueue> findAllByReceiverPhoneNum(String receiverPhoneNum);
}

package com.soi.backend.domain.friend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter

@Table(name = "friend_request_queue", schema = "soi")
public class FriendRequestQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "receiver_phone", nullable = false)
    private String receiverPhoneNum;

    public FriendRequestQueue(Long requesterId, String receiverPhoneNum) {
        this.requesterId = requesterId;
        this.receiverPhoneNum = receiverPhoneNum;
    }
}

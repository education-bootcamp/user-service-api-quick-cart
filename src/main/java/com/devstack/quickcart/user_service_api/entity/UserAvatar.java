package com.devstack.quickcart.user_service_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "user_avatar")
public class UserAvatar {
    @Id
    @Column(nullable = false, unique = true, name="avatar_id")
    private String avatarId;
    @Embedded
    private FileResource fileResource;

    //============================
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}

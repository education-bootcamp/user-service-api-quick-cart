package com.devstack.quickcart.user_service_api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
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

package com.devstack.quickcart.user_service_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "user")
public class User {
    @Id
    @Column(unique = true, nullable = false, name = "user_id")
    private String userId;
    @Column(unique = true, nullable = false, name = "username", length = 100)
    private String username;
    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;
    @Column(name = "last_name", length = 50, nullable = false)
    private String lastName;
    @Column(name = "active_status", columnDefinition = "TINYINT")
    private boolean activeState;
    //===================
    @OneToOne(mappedBy = "user")
    private ShippingAddress shippingAddress;

    //===================
    @OneToOne(mappedBy = "user")
    private BillingAddress billingAddress;

    //===================
    @OneToOne(mappedBy = "user")
    private UserAvatar userAvatar;

    @Column(name = "is_account_non_expired", columnDefinition = "TINYINT", nullable = false)
    private Boolean isAccountNonExpired;

    @Column(name = "is_email_verified", columnDefinition = "TINYINT", nullable = false)
    private Boolean isEmailVerified;

    @Column(name = "is_account_non_locked", columnDefinition = "TINYINT", nullable = false)
    private Boolean isAccountNonLocked;

    @Column(name = "is_enabled", columnDefinition = "TINYINT", nullable = false)
    private Boolean isEnabled;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", nullable = false, columnDefinition = "DATETIME")
    private Date createdDate;

    @OneToOne(mappedBy = "systemUser", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private Otp otp;

}

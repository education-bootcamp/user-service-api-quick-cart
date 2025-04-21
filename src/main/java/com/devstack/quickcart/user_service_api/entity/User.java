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
@Table(name = "user")
public class User {
    @Id
    @Column(unique = true, nullable = false, name = "user_id")
    private String userId;
    @Column(unique = true, nullable = false, name = "user_id", length = 100)
    private String username;
    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;
    @Column(name = "last_name", length = 50, nullable = false)
    private String lastName;
    @Column(name = "active_status", columnDefinition = "TINYINT")
    private boolean activeState;
    @Column(name = "otp", nullable = false)
    private int otp;

    //===================
    @OneToOne(mappedBy = "user")
    private ShippingAddress shippingAddress;

    //===================
    @OneToOne(mappedBy = "user")
    private BillingAddress billingAddress;

    //===================
    @OneToOne(mappedBy = "user")
    private UserAvatar userAvatar;

}

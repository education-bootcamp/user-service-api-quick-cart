package com.devstack.system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "system_user")
public class SystemUser {
    @Id
    @Column(name = "property_id", nullable = false, length = 80)
    private String propertyId;

    @Column(name = "active_state", columnDefinition = "TINYINT", nullable = false)
    private Boolean activeState;

    @Column(name = "email", unique = true, length = 250, nullable = false)
    private String email;

    @Column(name = "first_name", length = 45, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 45, nullable = false)
    private String lastName;

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

    @OneToOne(mappedBy = "systemUser", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private SystemUserAvatar systemUserAvatar;
}

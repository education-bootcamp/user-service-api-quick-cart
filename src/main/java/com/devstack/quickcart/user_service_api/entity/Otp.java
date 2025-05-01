package com.devstack.quickcart.user_service_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@ToString
@Table(name = "otp")
public class Otp {
    @Id
    @Column(name = "property_id", nullable = false, length = 80)
    private String propertyId;
    @Column(name = "code", nullable = false, length = 10)
    private String code;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", columnDefinition = "DATETIME", nullable = false)
    private Date createdDate;
    @Column(name = "is_verified", nullable = false, columnDefinition = "TINYINT")
    private Boolean isVerified;
    @Column(name = "attempts", nullable = false)
    private Integer attempts;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_property_id", referencedColumnName = "property_id", nullable = false)
    private User systemUser;
}

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
@Table(name = "system_user_avatar")
public class SystemUserAvatar {
    @Id
    @Column(name = "property_id", nullable = false, length = 80)
    private String propertyId;

    @Lob
    @Column(name = "directory", nullable = false)
    private byte[] directory;

    @Lob
    @Column(name = "file_name", nullable = false)
    private byte[] fileName;

    @Lob
    @Column(name = "resource_url", nullable = false)
    private byte[] resourceUrl;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", nullable = false, columnDefinition = "DATETIME")
    private Date createdDate;

    @Lob
    @Column(name = "hash", nullable = false)
    private byte[] hash;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_property_id", referencedColumnName = "property_id", nullable = false)
    private SystemUser systemUser;
}

package com.devstack.quickcart.user_service_api.repo;

import com.devstack.quickcart.user_service_api.entity.BillingAddress;
import com.devstack.quickcart.user_service_api.entity.UserAvatar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAvatarRepo extends JpaRepository<UserAvatar, String> {
}

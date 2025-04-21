package com.devstack.quickcart.user_service_api.repo;

import com.devstack.quickcart.user_service_api.entity.ShippingAddress;
import com.devstack.quickcart.user_service_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, String> {
}

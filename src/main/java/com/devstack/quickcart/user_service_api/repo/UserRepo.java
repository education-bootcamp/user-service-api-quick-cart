package com.devstack.quickcart.user_service_api.repo;

import com.devstack.quickcart.user_service_api.entity.ShippingAddress;
import com.devstack.quickcart.user_service_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, String> {
    public Optional<User> findByUsername(String email);
}

package com.devstack.system.repository;

import com.devstack.system.entity.SystemUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Optional;

@EnableJpaRepositories
public interface SystemUserRepo extends JpaRepository<SystemUser,String> {
    public Optional<SystemUser> findByEmail(String email);
}

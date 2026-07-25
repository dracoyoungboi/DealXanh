package com.dealxanh.app.repository;

import com.dealxanh.app.entity.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppConfigRepository extends JpaRepository<AppConfig, String> {
    Optional<AppConfig> findByConfigKey(String configKey);
}

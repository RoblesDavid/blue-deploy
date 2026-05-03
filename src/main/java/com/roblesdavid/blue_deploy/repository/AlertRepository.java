package com.roblesdavid.blue_deploy.repository;

import com.roblesdavid.blue_deploy.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
}

package com.roblesdavid.blue_deploy.repository;

import com.roblesdavid.blue_deploy.model.Deployment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeploymentRepository extends JpaRepository<Deployment, Long> {

    Optional<Deployment> findByDeploymentId(String deploymentId);
}
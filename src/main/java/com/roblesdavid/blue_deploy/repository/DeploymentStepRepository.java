package com.roblesdavid.blue_deploy.repository;

import com.roblesdavid.blue_deploy.model.Deployment;
import com.roblesdavid.blue_deploy.model.DeploymentStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeploymentStepRepository extends JpaRepository<DeploymentStep, Long> {

    List<DeploymentStep> findByDeploymentOrderByReportedAtAsc(Deployment deployment);
}

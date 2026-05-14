package com.roblesdavid.blue_deploy.repository;

import com.roblesdavid.blue_deploy.model.CheckStatus;
import com.roblesdavid.blue_deploy.model.Deployment;
import com.roblesdavid.blue_deploy.model.SecurityCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SecurityCheckRepository extends JpaRepository<SecurityCheck, Long> {
    List<SecurityCheck> findByDeploymentOrderByReportedAtAsc(Deployment deployment);

}

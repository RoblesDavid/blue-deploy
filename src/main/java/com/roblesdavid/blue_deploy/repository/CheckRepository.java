package com.roblesdavid.blue_deploy.repository;

import com.roblesdavid.blue_deploy.model.Check;

import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Repositorio JPA para checks del endpoint legacy.
 */
public interface CheckRepository extends JpaRepository<Check, Long> {

}

package com.coursework.office_equipment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coursework.office_equipment.model.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String name);
}
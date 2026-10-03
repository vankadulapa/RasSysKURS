package com.coursework.office_equipment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coursework.office_equipment.model.AppUser;

public interface UserRepository extends JpaRepository<AppUser, Integer> {
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByEmployeeId(Integer employeeId);
}
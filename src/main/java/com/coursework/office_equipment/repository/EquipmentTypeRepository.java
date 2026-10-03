package com.coursework.office_equipment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coursework.office_equipment.model.EquipmentType;

public interface EquipmentTypeRepository extends JpaRepository<EquipmentType, Integer> {}
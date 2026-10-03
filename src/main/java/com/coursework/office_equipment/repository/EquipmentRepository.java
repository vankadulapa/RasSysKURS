package com.coursework.office_equipment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.coursework.office_equipment.model.Equipment;

public interface EquipmentRepository extends JpaRepository<Equipment, Integer> {

    @Query("""
        select e from Equipment e
        where (lower(e.name) like lower(concat('%', :q, '%'))
            or lower(e.inventoryNumber) like lower(concat('%', :q, '%'))
            or lower(coalesce(e.ipAddress, '')) like lower(concat('%', :q, '%'))
            or lower(coalesce(e.macAddress, '')) like lower(concat('%', :q, '%'))
            or lower(coalesce(e.location, '')) like lower(concat('%', :q, '%')))
          and (:status = '' or e.status = :status)
        order by e.inventoryNumber
        """)
    List<Equipment> search(@Param("q") String q, @Param("status") String status);
}
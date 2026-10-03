package com.coursework.office_equipment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.coursework.office_equipment.model.Department;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    @Query("""
        select d from Department d
        where lower(d.name) like lower(concat('%', :q, '%'))
           or lower(coalesce(d.room, '')) like lower(concat('%', :q, '%'))
        order by d.name
        """)
    List<Department> search(@Param("q") String q);
}
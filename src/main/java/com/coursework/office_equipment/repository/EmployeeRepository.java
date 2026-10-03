package com.coursework.office_equipment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.coursework.office_equipment.model.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    @Query("""
        select e from Employee e
        where lower(e.fullName) like lower(concat('%', :q, '%'))
           or lower(coalesce(e.position, '')) like lower(concat('%', :q, '%'))
           or lower(coalesce(e.phone, '')) like lower(concat('%', :q, '%'))
           or lower(e.department.name) like lower(concat('%', :q, '%'))
        order by e.fullName
        """)
    List<Employee> search(@Param("q") String q);
}
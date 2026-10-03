package com.coursework.office_equipment;

import com.coursework.office_equipment.model.AppUser;
import com.coursework.office_equipment.model.Department;
import com.coursework.office_equipment.model.Employee;
import com.coursework.office_equipment.model.Equipment;
import com.coursework.office_equipment.model.EquipmentType;
import com.coursework.office_equipment.model.Role;

public final class TestData {

    private TestData() {}

    public static Department department(int id, String name) {
        Department d = new Department();
        d.setId(id);
        d.setName(name);
        return d;
    }

    public static Employee employee(int id, String fullName, Department department) {
        Employee e = new Employee();
        e.setId(id);
        e.setFullName(fullName);
        e.setDepartment(department);
        return e;
    }

    public static EquipmentType type(int id, String name) {
        EquipmentType t = new EquipmentType();
        t.setId(id);
        t.setName(name);
        return t;
    }

    public static Equipment equipment(int id, String inventoryNumber, EquipmentType type, String status) {
        Equipment e = new Equipment();
        e.setId(id);
        e.setInventoryNumber(inventoryNumber);
        e.setName("Устройство " + inventoryNumber);
        e.setType(type);
        e.setStatus(status);
        return e;
    }

    public static Role role(int id, String name) {
        Role r = new Role();
        r.setId(id);
        r.setName(name);
        return r;
    }

    public static AppUser user(int id, String username, Role role) {
        AppUser u = new AppUser();
        u.setId(id);
        u.setUsername(username);
        u.setPasswordHash("hash");
        u.setRole(role);
        u.setActive(true);
        return u;
    }
}
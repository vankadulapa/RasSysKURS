package com.coursework.office_equipment.controller;

import com.coursework.office_equipment.TestData;
import com.coursework.office_equipment.model.Employee;
import com.coursework.office_equipment.model.Equipment;
import com.coursework.office_equipment.model.EquipmentType;
import com.coursework.office_equipment.repository.EmployeeRepository;
import com.coursework.office_equipment.repository.EquipmentRepository;
import com.coursework.office_equipment.repository.EquipmentTypeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EquipmentControllerTest {

    private final EquipmentRepository equipmentRepository = mock(EquipmentRepository.class);
    private final EquipmentTypeRepository typeRepository = mock(EquipmentTypeRepository.class);
    private final EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
    private final EquipmentController controller =
            new EquipmentController(equipmentRepository, typeRepository, employeeRepository);

    private final EquipmentType pc = TestData.type(1, "ПК");

    private Equipment saved() {
        ArgumentCaptor<Equipment> captor = ArgumentCaptor.forClass(Equipment.class);
        verify(equipmentRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void listCountsEquipmentByStatus() {
        when(equipmentRepository.findAll()).thenReturn(List.of(
                TestData.equipment(1, "INV-1", pc, "IN_USE"),
                TestData.equipment(2, "INV-2", pc, "IN_USE"),
                TestData.equipment(3, "INV-3", pc, "IN_STOCK"),
                TestData.equipment(4, "INV-4", pc, "IN_REPAIR")));
        Model model = new ExtendedModelMap();

        String view = controller.list("", "", model);

        assertEquals("equipment/list", view);
        assertEquals(4, model.asMap().get("statTotal"));
        assertEquals(2L, model.asMap().get("statInUse"));
        assertEquals(1L, model.asMap().get("statInStock"));
        assertEquals(1L, model.asMap().get("statInRepair"));
    }

    @Test
    void saveTurnsBlankFieldsIntoNull() {
        Equipment e = TestData.equipment(1, "INV-1", pc, "IN_USE");
        e.setIpAddress("   ");
        e.setMacAddress("");
        e.setSerialNumber(" SN-1 ");

        String view = controller.save(e);

        Equipment s = saved();
        assertNull(s.getIpAddress());
        assertNull(s.getMacAddress());
        assertEquals("SN-1", s.getSerialNumber());
        assertEquals("redirect:/equipment", view);
    }

    @Test
    void saveClearsOwnerWithoutId() {
        Equipment e = TestData.equipment(1, "INV-1", pc, "IN_STOCK");
        e.setOwner(new Employee());

        controller.save(e);

        assertNull(saved().getOwner());
    }

    @Test
    void deleteRemovesAndRedirects() {
        String view = controller.delete(7);

        verify(equipmentRepository).deleteById(7);
        assertEquals("redirect:/equipment", view);
    }
}
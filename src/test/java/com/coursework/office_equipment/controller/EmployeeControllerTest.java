package com.coursework.office_equipment.controller;

import com.coursework.office_equipment.TestData;
import com.coursework.office_equipment.model.Department;
import com.coursework.office_equipment.model.Employee;
import com.coursework.office_equipment.repository.DepartmentRepository;
import com.coursework.office_equipment.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeControllerTest {

    private final EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
    private final DepartmentRepository departmentRepository = mock(DepartmentRepository.class);
    private final EmployeeController controller =
            new EmployeeController(employeeRepository, departmentRepository);
    private final RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
    private final Department it = TestData.department(1, "IT");

    @Test
    void addRejectsBlankName() {
        String view = controller.add("  ", "Инженер", "", 1, ra);

        assertEquals("redirect:/employees", view);
        assertNotNull(ra.getFlashAttributes().get("error"));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void addSavesEmployeeWithDepartment() {
        when(departmentRepository.findById(1)).thenReturn(Optional.of(it));

        controller.add("  Иванов Иван ", " ", " +7 900 ", 1, ra);

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        Employee e = captor.getValue();
        assertEquals("Иванов Иван", e.getFullName());
        assertNull(e.getPosition());
        assertEquals("+7 900", e.getPhone());
        assertSame(it, e.getDepartment());
    }

    @Test
    void addFailsForUnknownDepartment() {
        assertThrows(NoSuchElementException.class,
                () -> controller.add("Иванов Иван", "", "", 99, ra));
    }

    @Test
    void deleteRemovesEmployee() {
        String view = controller.delete(4, ra);

        verify(employeeRepository).deleteById(4);
        assertEquals("redirect:/employees", view);
        assertNotNull(ra.getFlashAttributes().get("message"));
    }

    @Test
    void deleteReportsEmployeeWithEquipmentOrAccount() {
        doThrow(new DataIntegrityViolationException("fk")).when(employeeRepository).deleteById(4);

        controller.delete(4, ra);

        assertNotNull(ra.getFlashAttributes().get("error"));
        assertNull(ra.getFlashAttributes().get("message"));
    }

    @Test
    void listAddsEmployeesAndDepartments() {
        Employee e = TestData.employee(1, "Анна", it);
        when(employeeRepository.search("анн")).thenReturn(List.of(e));
        when(departmentRepository.findAll()).thenReturn(List.of(it));
        Model model = new ExtendedModelMap();

        String view = controller.list(" анн ", model);

        assertEquals("employees", view);
        assertEquals(List.of(e), model.asMap().get("items"));
        assertEquals(List.of(it), model.asMap().get("departments"));
    }
}
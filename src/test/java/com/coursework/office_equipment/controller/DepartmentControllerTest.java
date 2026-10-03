package com.coursework.office_equipment.controller;

import com.coursework.office_equipment.TestData;
import com.coursework.office_equipment.model.Department;
import com.coursework.office_equipment.repository.DepartmentRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DepartmentControllerTest {

    private final DepartmentRepository repo = mock(DepartmentRepository.class);
    private final DepartmentController controller = new DepartmentController(repo);
    private final RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

    private Department saved() {
        ArgumentCaptor<Department> captor = ArgumentCaptor.forClass(Department.class);
        verify(repo).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void addRejectsBlankName() {
        String view = controller.add("  ", "101", ra);

        assertEquals("redirect:/departments", view);
        assertNotNull(ra.getFlashAttributes().get("error"));
        verify(repo, never()).save(any());
    }

    @Test
    void addTrimsNameAndStoresBlankRoomAsNull() {
        controller.add("  Бухгалтерия ", "  ", ra);

        Department d = saved();
        assertEquals("Бухгалтерия", d.getName());
        assertNull(d.getRoom());
    }

    @Test
    void addKeepsRoom() {
        controller.add("IT", " 205 ", ra);

        assertEquals("205", saved().getRoom());
    }

    @Test
    void deleteRemovesDepartment() {
        String view = controller.delete(3, ra);

        verify(repo).deleteById(3);
        assertEquals("redirect:/departments", view);
        assertNotNull(ra.getFlashAttributes().get("message"));
    }

    @Test
    void deleteReportsDepartmentWithEmployees() {
        doThrow(new DataIntegrityViolationException("fk")).when(repo).deleteById(3);

        controller.delete(3, ra);

        assertNotNull(ra.getFlashAttributes().get("error"));
        assertNull(ra.getFlashAttributes().get("message"));
    }

    @Test
    void listUsesTrimmedQueryForSearch() {
        Department d = TestData.department(1, "Бухгалтерия");
        when(repo.search("бух")).thenReturn(List.of(d));
        Model model = new ExtendedModelMap();

        String view = controller.list("  бух ", model);

        assertEquals("departments", view);
        assertEquals(List.of(d), model.asMap().get("items"));
        assertEquals("  бух ", model.asMap().get("q"));
    }
}
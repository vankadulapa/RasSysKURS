package com.coursework.office_equipment.controller;

import com.coursework.office_equipment.TestData;
import com.coursework.office_equipment.model.Employee;
import com.coursework.office_equipment.model.Equipment;
import com.coursework.office_equipment.model.EquipmentType;
import com.coursework.office_equipment.repository.EmployeeRepository;
import com.coursework.office_equipment.repository.EquipmentRepository;
import com.coursework.office_equipment.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentControllerTest {

    private final EquipmentRepository equipmentRepository = mock(EquipmentRepository.class);
    private final EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final DocumentController controller =
            new DocumentController(equipmentRepository, employeeRepository, userRepository);

    private final EquipmentType pc = TestData.type(1, "ПК");

    private String printDocument(String type, List<Integer> ids, boolean apply, Integer toId,
                                 Model model, RedirectAttributesModelMap ra) {
        return controller.print(type, ids, "", null, "", "", null, toId, null, null, null,
                apply, model, ra);
    }

    @Test
    void printWithoutIdsRedirectsBackWithError() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String view = printDocument("waybill", null, false, null, new ExtendedModelMap(), ra);

        assertEquals("redirect:/documents?type=waybill", view);
        assertNotNull(ra.getFlashAttributes().get("error"));
        verifyNoInteractions(equipmentRepository);
    }

    @Test
    void waybillWithApplyAssignsOwnerAndMovesStockItemsToInUse() {
        Equipment stock = TestData.equipment(1, "INV-1", pc, "IN_STOCK");
        Equipment repair = TestData.equipment(2, "INV-2", pc, "IN_REPAIR");
        Employee receiver = TestData.employee(10, "Пётр", TestData.department(1, "IT"));
        when(equipmentRepository.findAllById(List.of(1, 2))).thenReturn(List.of(stock, repair));
        when(employeeRepository.findById(10)).thenReturn(Optional.of(receiver));
        Model model = new ExtendedModelMap();

        String view = printDocument("waybill", List.of(1, 2), true, 10, model,
                new RedirectAttributesModelMap());

        assertEquals("document-print", view);
        assertSame(receiver, stock.getOwner());
        assertEquals("IN_USE", stock.getStatus());
        assertSame(receiver, repair.getOwner());
        assertEquals("IN_REPAIR", repair.getStatus());
        verify(equipmentRepository).saveAll(any());
        assertEquals(Boolean.TRUE, model.asMap().get("applied"));
    }

    @Test
    void writeOffWithApplyMarksEquipmentWrittenOff() {
        Equipment a = TestData.equipment(1, "INV-1", pc, "IN_REPAIR");
        when(equipmentRepository.findAllById(List.of(1))).thenReturn(List.of(a));
        Model model = new ExtendedModelMap();

        printDocument("writeoff", List.of(1), true, null, model, new RedirectAttributesModelMap());

        assertEquals("WRITTEN_OFF", a.getStatus());
        verify(equipmentRepository).saveAll(any());
        assertEquals(Boolean.TRUE, model.asMap().get("applied"));
    }
}
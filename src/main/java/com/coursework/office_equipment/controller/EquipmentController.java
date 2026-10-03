package com.coursework.office_equipment.controller;

import java.util.Arrays;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.coursework.office_equipment.model.Employee;
import com.coursework.office_equipment.model.Equipment;
import com.coursework.office_equipment.model.EquipmentStatus;
import com.coursework.office_equipment.model.EquipmentType;
import com.coursework.office_equipment.repository.EmployeeRepository;
import com.coursework.office_equipment.repository.EquipmentRepository;
import com.coursework.office_equipment.repository.EquipmentTypeRepository;

@Controller
public class EquipmentController {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentTypeRepository typeRepository;
    private final EmployeeRepository employeeRepository;

    public EquipmentController(EquipmentRepository equipmentRepository,
                               EquipmentTypeRepository typeRepository,
                               EmployeeRepository employeeRepository) {
        this.equipmentRepository = equipmentRepository;
        this.typeRepository = typeRepository;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping({"/", "/equipment"})
    public String list(@RequestParam(defaultValue = "") String q,
                   @RequestParam(defaultValue = "") String status,
                   Model model) {
    var all = equipmentRepository.findAll();
    model.addAttribute("statTotal", all.size());
    model.addAttribute("statInUse", all.stream().filter(e -> "IN_USE".equals(e.getStatus())).count());
    model.addAttribute("statInStock", all.stream().filter(e -> "IN_STOCK".equals(e.getStatus())).count());
    model.addAttribute("statInRepair", all.stream().filter(e -> "IN_REPAIR".equals(e.getStatus())).count());
    model.addAttribute("items", equipmentRepository.search(q.trim(), status));
    model.addAttribute("q", q);
    model.addAttribute("status", status);
    model.addAttribute("statuses",
            Arrays.stream(EquipmentStatus.values()).map(Enum::name).toList());
    return "equipment/list";
}


    @GetMapping("/equipment/new")
    public String createForm(Model model) {
        Equipment e = new Equipment();
        e.setStatus("IN_STOCK");
        e.setType(new EquipmentType());
        e.setOwner(new Employee());
        return form(e, model);
    }

    @GetMapping("/equipment/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        Equipment e = equipmentRepository.findById(id).orElseThrow();
        if (e.getOwner() == null) {
            e.setOwner(new Employee());
        }
        return form(e, model);
    }

    @PostMapping("/equipment/save")
    public String save(@ModelAttribute Equipment equipment) {
        if (equipment.getOwner() != null && equipment.getOwner().getId() == null) {
            equipment.setOwner(null);
        }
        equipment.setSerialNumber(blankToNull(equipment.getSerialNumber()));
        equipment.setIpAddress(blankToNull(equipment.getIpAddress()));
        equipment.setMacAddress(blankToNull(equipment.getMacAddress()));
        equipment.setLocation(blankToNull(equipment.getLocation()));
        equipment.setSpecs(blankToNull(equipment.getSpecs()));
        equipmentRepository.save(equipment);
        return "redirect:/equipment";
    }

    @PostMapping("/equipment/{id}/delete")
    public String delete(@PathVariable Integer id) {
        equipmentRepository.deleteById(id);
        return "redirect:/equipment";
    }

    private String form(Equipment equipment, Model model) {
        model.addAttribute("equipment", equipment);
        model.addAttribute("types", typeRepository.findAll());
        model.addAttribute("employees", employeeRepository.findAll());
        model.addAttribute("statuses",
                Arrays.stream(EquipmentStatus.values()).map(Enum::name).toList());
        return "equipment/form";
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
package com.coursework.office_equipment.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.coursework.office_equipment.model.Employee;
import com.coursework.office_equipment.repository.DepartmentRepository;
import com.coursework.office_equipment.repository.EmployeeRepository;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeController(EmployeeRepository employeeRepository,
                              DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("items", employeeRepository.search(q.trim()));
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("q", q);
        return "employees";
    }

    @PostMapping("/add")
    public String add(@RequestParam String fullName,
                      @RequestParam(defaultValue = "") String position,
                      @RequestParam(defaultValue = "") String phone,
                      @RequestParam Integer departmentId, RedirectAttributes ra) {
        if (fullName.isBlank()) {
            ra.addFlashAttribute("error", "ФИО сотрудника не должно быть пустым");
        } else {
            Employee e = new Employee();
            e.setFullName(fullName.trim());
            e.setPosition(position.isBlank() ? null : position.trim());
            e.setPhone(phone.isBlank() ? null : phone.trim());
            e.setDepartment(departmentRepository.findById(departmentId).orElseThrow());
            employeeRepository.save(e);
            ra.addFlashAttribute("message", "Сотрудник добавлен");
        }
        return "redirect:/employees";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            employeeRepository.deleteById(id);
            ra.addFlashAttribute("message", "Сотрудник удалён");
        } catch (DataIntegrityViolationException ex) {
            ra.addFlashAttribute("error",
                    "Нельзя удалить: за сотрудником закреплена техника или есть учётная запись");
        }
        return "redirect:/employees";
    }
}
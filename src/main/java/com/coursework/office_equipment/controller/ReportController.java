package com.coursework.office_equipment.controller;

import com.coursework.office_equipment.model.Equipment;
import com.coursework.office_equipment.repository.EquipmentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class ReportController {

    private final EquipmentRepository equipmentRepository;

    public ReportController(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    @GetMapping("/reports")
    public String reports(@RequestParam(defaultValue = "departments") String type,
                          @RequestParam(defaultValue = "5") int years,
                          Model model) {
        List<Equipment> all = equipmentRepository.findAll();
        LocalDate today = LocalDate.now();
        int minYears = Math.max(years, 0);
        String title;

        switch (type) {
            case "types" -> {
                title = "Техника по типам и статусам";
                model.addAttribute("rows", groupRows(all, e -> e.getType().getName()));
            }
            case "network" -> {
                title = "Сетевой реестр (устройства с IP или MAC)";
                model.addAttribute("items", all.stream()
                        .filter(e -> e.getIpAddress() != null || e.getMacAddress() != null)
                        .sorted(Comparator.comparingLong((Equipment e) -> ipKey(e.getIpAddress())))
                        .toList());
            }
            case "unassigned" -> {
                title = "Техника без владельца";
                model.addAttribute("items", all.stream()
                        .filter(e -> e.getOwner() == null)
                        .sorted(Comparator.comparing(Equipment::getInventoryNumber))
                        .toList());
            }
            case "old" -> {
                title = "Техника старше " + minYears + " лет";
                List<Equipment> old = all.stream()
                        .filter(e -> !"WRITTEN_OFF".equals(e.getStatus()))
                        .filter(e -> e.getPurchaseDate() != null
                                && e.getPurchaseDate().isBefore(today.minusYears(minYears)))
                        .sorted(Comparator.comparing(Equipment::getPurchaseDate))
                        .toList();
                Map<Integer, Long> ages = new HashMap<>();
                old.forEach(e -> ages.put(e.getId(),
                        ChronoUnit.YEARS.between(e.getPurchaseDate(), today)));
                model.addAttribute("items", old);
                model.addAttribute("ages", ages);
            }
            default -> {
                type = "departments";
                title = "Техника по подразделениям";
                model.addAttribute("rows", groupRows(all, e -> e.getOwner() == null
                        ? "Без владельца"
                        : e.getOwner().getDepartment().getName()));
            }
        }

        model.addAttribute("type", type);
        model.addAttribute("title", title);
        model.addAttribute("years", minYears);
        model.addAttribute("today", today.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        return "reports";
    }

    private static List<Row> groupRows(List<Equipment> all, Function<Equipment, String> key) {
        Map<String, List<Equipment>> groups = all.stream()
                .collect(Collectors.groupingBy(key, TreeMap::new, Collectors.toList()));
        List<Row> rows = new ArrayList<>();
        groups.forEach((name, list) -> rows.add(row(name, list)));
        rows.add(row("Итого", all));
        return rows;
    }

    private static Row row(String name, List<Equipment> list) {
        return new Row(name, list.size(),
                count(list, "IN_USE"), count(list, "IN_STOCK"),
                count(list, "IN_REPAIR"), count(list, "WRITTEN_OFF"));
    }

    private static long count(List<Equipment> list, String status) {
        return list.stream().filter(e -> status.equals(e.getStatus())).count();
    }

    // IPv4 -> число, чтобы 192.168.1.2 шёл раньше 192.168.1.11
    private static long ipKey(String ip) {
        if (ip == null) {
            return Long.MAX_VALUE;
        }
        try {
            long result = 0;
            for (String part : ip.split("\\.")) {
                result = result * 256 + Integer.parseInt(part.trim());
            }
            return result;
        } catch (NumberFormatException ex) {
            return Long.MAX_VALUE - 1;
        }
    }

    public static class Row {
        public final String name;
        public final long total;
        public final long inUse;
        public final long inStock;
        public final long inRepair;
        public final long writtenOff;

        public Row(String name, long total, long inUse, long inStock, long inRepair, long writtenOff) {
            this.name = name;
            this.total = total;
            this.inUse = inUse;
            this.inStock = inStock;
            this.inRepair = inRepair;
            this.writtenOff = writtenOff;
        }
    }
}
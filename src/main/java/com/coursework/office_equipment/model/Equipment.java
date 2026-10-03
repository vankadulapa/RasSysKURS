package com.coursework.office_equipment.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "equipment")
public class Equipment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String inventoryNumber;
    private String name;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private EquipmentType type;

    private String serialNumber;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate purchaseDate;

    private String status;

    private String ipAddress;
    private String macAddress;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Employee owner;

    private String location;
    private String specs;

    

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getInventoryNumber() { return inventoryNumber; }
    public void setInventoryNumber(String v) { this.inventoryNumber = v; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public EquipmentType getType() { return type; }
    public void setType(EquipmentType type) { this.type = type; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String v) { this.serialNumber = v; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate v) { this.purchaseDate = v; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String v) { this.ipAddress = v; }
    public String getMacAddress() { return macAddress; }
    public void setMacAddress(String v) { this.macAddress = v; }
    public Employee getOwner() { return owner; }
    public void setOwner(Employee owner) { this.owner = owner; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getSpecs() { return specs; }
    public void setSpecs(String specs) { this.specs = specs; }
}
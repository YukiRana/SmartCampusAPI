package com.mycompany.smartcampusapi.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "sensors")
public class Sensor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 80)
    private String type;
    
    @Column(name = "current_value")
    private Double currentValue;
    
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
    
    @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reading> readings = new ArrayList<>();

    public void addReading(Reading reading) {
        readings.add(reading);
        reading.setSensor(this);
    }

    public void removeReading(Reading reading) {
        readings.remove(reading);
        reading.setSensor(null);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public Double getCurrentValue() { return currentValue; }
    public void setCurrentValue(Double currentValue) { this.currentValue = currentValue; }
    public List<Reading> getReadings() { return readings; }
    public void setReadings(List<Reading> readings) { this.readings = readings; }
}

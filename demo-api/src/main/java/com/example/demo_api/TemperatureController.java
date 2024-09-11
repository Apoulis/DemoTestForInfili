package com.example.demo_api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/temperature")
public class TemperatureController {

    @Autowired
    private TemperatureRepository temperatureRepository;

    @PostMapping
    public ResponseEntity<Temperature> createTemperature(@RequestBody Temperature temperature) {
        temperature.setDatetime(LocalDateTime.now());
        Temperature savedTemperature = temperatureRepository.save(temperature);
        return new ResponseEntity<>(savedTemperature, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Temperature> getTemperature(@PathVariable Long id) {
        Optional<Temperature> temperature = temperatureRepository.findById(id);
        return temperature.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemperature(@PathVariable Long id) {
        if (temperatureRepository.existsById(id)) {
            temperatureRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

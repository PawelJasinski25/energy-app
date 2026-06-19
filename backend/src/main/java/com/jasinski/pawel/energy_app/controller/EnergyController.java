package com.jasinski.pawel.energy_app.controller;

import com.jasinski.pawel.energy_app.dto.ChargingWindow;
import com.jasinski.pawel.energy_app.dto.DailyEnergyMix;
import com.jasinski.pawel.energy_app.service.EnergyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/energy")
public class EnergyController {

    private final EnergyService energyService;

    public EnergyController(EnergyService energyService) {
        this.energyService = energyService;
    }

    @GetMapping("/mix")
    public ResponseEntity<List<DailyEnergyMix>> getEnergyMixForThreeDays() {
        List<DailyEnergyMix> result = energyService.getEnergyMixForThreeDays();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/optimal-window")
    public ResponseEntity<ChargingWindow> getOptimalChargingWindow(@RequestParam int hours) {
        ChargingWindow result = energyService.getOptimalChargingWindow(hours);
        return ResponseEntity.ok(result);
    }
}
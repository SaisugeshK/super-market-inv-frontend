package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.SettingRequest;
import com.example.InventoryManagementSystem.dto.SettingResponse;
import com.example.InventoryManagementSystem.service.SettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService service;

    @PostMapping
    public ResponseEntity<SettingResponse> createSetting(
            @RequestBody SettingRequest request
    ) {

        return ResponseEntity.ok(
                service.createSetting(request)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<SettingResponse> updateSetting(
            @PathVariable Long id,
            @RequestBody SettingRequest request
    ) {

        return ResponseEntity.ok(
                service.updateSetting(id, request)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SettingResponse> getSettingById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                service.getSettingById(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<SettingResponse>> getAllSettings() {

        return ResponseEntity.ok(
                service.getAllSettings()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSetting(
            @PathVariable Long id
    ) {

        service.deleteSetting(id);

        return ResponseEntity.ok("Setting Deleted Successfully");
    }
}
package com.cibertec.controller;

import com.cibertec.service.CountryService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/countries")
@RequiredArgsConstructor
public class CountryController {

    private final CountryService service;

    @GetMapping("/{code}")
    public ResponseEntity<?> getByCode(
            @PathVariable
            @NotBlank(message = "El código no puede estar vacío")
            String code) {

        return ResponseEntity.ok(service.getByCode(code));
    }

    @GetMapping("/search")
    public ResponseEntity<List<?>> searchByName(
            @RequestParam
            @NotBlank(message = "El nombre no puede estar vacío")
            String name) {

        return ResponseEntity.ok(service.searchByName(name));
    }

    @GetMapping("/all")
    public ResponseEntity<List<?>> getAllCountries() {
        return ResponseEntity.ok(service.getAllCountries());
    }

    @GetMapping("/region/{region}")
    public ResponseEntity<List<?>> getByRegion(
            @PathVariable String region) {

        return ResponseEntity.ok(service.getByRegion(region));
    }
}
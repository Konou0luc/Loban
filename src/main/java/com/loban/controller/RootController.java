package com.loban.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping(value = {"/", "/api"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> root() {
        return ResponseEntity.ok(
                Map.of(
                        "service", "Loban API",
                        "status", "ok",
                        "message", "Le serveur répond correctement."));
    }
}

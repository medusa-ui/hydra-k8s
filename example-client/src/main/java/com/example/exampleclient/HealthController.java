package com.example.exampleclient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping(value={"/health"})
    public Map<String, String> healthCheck() {
        return Map.of("status", "ok");
    }

}

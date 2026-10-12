package com.ilque.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import com.ilque.global.response.ApiResponseEnvelope;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ApiResponseEnvelope<Map<String, String>> health() {
        return ApiResponseEnvelope.success(Map.of("status", "UP"));
    }
}

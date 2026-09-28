package com.vimeanbaby.common;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public ApiResponse<Map<String, String>> root() {
        return ApiResponse.ok(Map.of(
                "name", "Vimean Baby API",
                "status", "UP",
                "docs", "/swagger-ui.html",
                "health", "/actuator/health"
        ));
    }
}

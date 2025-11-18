package Transport_Urbain_Microservices.auth_test_service.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/hello")
    public Map<String, Object> hello(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from the test Microservice");
        response.put("user", jwt.getClaimAsString("preferred_username"));
        response.put("subject", jwt.getSubject());
        response.put("allClaims", jwt.getClaims());
        return response;
    }

    @GetMapping("/public/info")
    public Map<String, String> publicInfo() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "This is a public endpoint of the test auth-test-service");
        response.put("service", "auth-test-service");
        return response;
    }

    @GetMapping("/very-cool/info")
    public Map<String, String> veryCoolInfo() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "This endpoint is only accessible for those with a very cool role");
        response.put("service", "auth-test-service");
        return response;
    }
}

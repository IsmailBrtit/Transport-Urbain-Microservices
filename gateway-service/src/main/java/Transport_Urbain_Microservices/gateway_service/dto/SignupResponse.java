package Transport_Urbain_Microservices.gateway_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SignupResponse {
    private boolean success;
    private String message;
    private String userId;
    private String username;
}

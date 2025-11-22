package Transport_Urbain_Microservices.user_service.exception;

public class AuthServiceException extends RuntimeException {
    public AuthServiceException(String message) {
        super(message);
    }
}

package Transport_Urbain_Microservices.user_service.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String s) {
        super(s);
    }
}

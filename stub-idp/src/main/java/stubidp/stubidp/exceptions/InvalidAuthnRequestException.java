package stubidp.stubidp.exceptions;

public class InvalidAuthnRequestException extends RuntimeException {

    public InvalidAuthnRequestException(String messsage) {
        super(messsage);
    }

    public InvalidAuthnRequestException(Exception exception) {
        super(exception.getMessage(), exception);
    }

    public InvalidAuthnRequestException(String messsage, Exception e) {
        super(messsage, e);
    }
}

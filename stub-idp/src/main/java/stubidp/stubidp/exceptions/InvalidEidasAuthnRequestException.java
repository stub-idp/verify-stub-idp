package stubidp.stubidp.exceptions;

public class InvalidEidasAuthnRequestException extends RuntimeException {

    public InvalidEidasAuthnRequestException(String messsage) {
        super(messsage);
    }

    public InvalidEidasAuthnRequestException(Exception exception) {
        super(exception.getMessage(), exception);
    }

    public InvalidEidasAuthnRequestException(String messsage, Exception e) {
        super(messsage, e);
    }
}

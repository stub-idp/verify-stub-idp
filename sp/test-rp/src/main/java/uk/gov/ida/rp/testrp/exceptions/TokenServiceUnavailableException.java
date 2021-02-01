package uk.gov.ida.rp.testrp.exceptions;

public class TokenServiceUnavailableException extends RuntimeException {
    public TokenServiceUnavailableException(String message) {
        super(message);
    }
}

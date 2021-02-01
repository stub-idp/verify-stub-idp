package uk.gov.ida.rp.testrp.exceptions;

public class InvalidAccessTokenException extends RuntimeException {
    public InvalidAccessTokenException(String message) {
        super(message);
    }
}

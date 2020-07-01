package stubidp.stubidp.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;

public class InvalidUserInDatabaseException extends RuntimeException {
    public InvalidUserInDatabaseException(JsonProcessingException e) {
        super(e);
    }
}

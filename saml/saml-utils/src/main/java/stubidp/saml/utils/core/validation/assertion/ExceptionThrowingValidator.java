package stubidp.saml.utils.core.validation.assertion;

@FunctionalInterface
public interface ExceptionThrowingValidator<T> {
    void apply(T t) throws ValidationException;

    class ValidationException extends Exception {
        public ValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
package uk.org.breakthemould.exception.domain;

public class PasswordValidationException extends RuntimeException{

    public PasswordValidationException() {
        super();
    }

    public PasswordValidationException(String message) {
        super(message);
    }

    public PasswordValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

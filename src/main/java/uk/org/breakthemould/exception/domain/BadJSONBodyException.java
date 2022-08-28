package uk.org.breakthemould.exception.domain;

public class BadJSONBodyException extends RuntimeException{

    public BadJSONBodyException() {
        super();
    }

    public BadJSONBodyException(String message) {
        super(message);
    }

    public BadJSONBodyException(String message, Throwable cause) {
        super(message, cause);
    }
}

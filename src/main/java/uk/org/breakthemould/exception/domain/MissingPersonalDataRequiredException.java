package uk.org.breakthemould.exception.domain;

public class MissingPersonalDataRequiredException extends RuntimeException{
    public MissingPersonalDataRequiredException() {
        super();
    }

    public MissingPersonalDataRequiredException(String message) {
        super(message);
    }

    public MissingPersonalDataRequiredException(String message, Throwable cause) {
        super(message, cause);
    }
}

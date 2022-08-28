package uk.org.breakthemould.exception.domain;

public class ActivityTemplateUniqueIDAlreadyExistsException extends RuntimeException{
    public ActivityTemplateUniqueIDAlreadyExistsException() {
        super();
    }

    public ActivityTemplateUniqueIDAlreadyExistsException(String message) {
        super(message);
    }

    public ActivityTemplateUniqueIDAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

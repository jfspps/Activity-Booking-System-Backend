package uk.org.breakthemould.exception.domain;

public class DateTimeException extends Exception{
    public DateTimeException() {
        super();
    }

    public DateTimeException(String message) {
        super(message);
    }

    public DateTimeException(String message, Throwable cause) {
        super(message, cause);
    }
}

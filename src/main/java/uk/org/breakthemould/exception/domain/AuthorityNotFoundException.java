package uk.org.breakthemould.exception.domain;

public class AuthorityNotFoundException extends RuntimeException{

    public AuthorityNotFoundException() {
        super();
    }

    public AuthorityNotFoundException(String message) {
        super(message);
    }

    public AuthorityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

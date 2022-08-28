package uk.org.breakthemould.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uk.org.breakthemould.domain.http.HttpResponse;
import uk.org.breakthemould.exception.domain.*;

import javax.persistence.NoResultException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Objects;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class APIExceptionHandler implements ErrorController {

    public static final String ACCOUNT_LOCKED = "Your account has been locked.";
    public static final String METHOD_IS_NOT_ALLOWED = "The request method is not allowed. '%s' was expected.";
    public static final String INTERNAL_SERVER_ERROR_MSG = "A server error occurred when processing the request.";
    public static final String INCORRECT_CREDENTIALS = "Username or password are missing or incorrect.";
    public static final String ACCOUNT_DISABLED = "Your account has been disabled.";
    public static final String FILE_PROCESSING_ERROR = "File processing error.";
    public static final String URL_FORMAT_ERROR = "URL parameter error";
    public static final String DATE_TIME_ERROR = "Start-date must occur before end-date";

    // invoke this when DisabledException is thrown (similar assumptions for other handlers)
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<HttpResponse> accountDisabledException(){
        return createHttpResponse(BAD_REQUEST, ACCOUNT_DISABLED);
    }

    @ExceptionHandler(ParseException.class)
    public ResponseEntity<HttpResponse> urlProcessingException(){
        return createHttpResponse(BAD_REQUEST, URL_FORMAT_ERROR);
    }

    @ExceptionHandler(DateTimeException.class)
    public ResponseEntity<HttpResponse> dateTimeProcessingException(){
        return createHttpResponse(BAD_REQUEST, DATE_TIME_ERROR);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<HttpResponse> notFoundException(NotFoundException e){
        return createHttpResponse(NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<HttpResponse> badCredentialsException() {
        return createHttpResponse(BAD_REQUEST, INCORRECT_CREDENTIALS);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<HttpResponse> authenticationException() {
        return createHttpResponse(UNAUTHORIZED, INCORRECT_CREDENTIALS);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<HttpResponse> accessDeniedException(AccessDeniedException e) {
        return createHttpResponse(FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<HttpResponse> lockedException() {
        return createHttpResponse(LOCKED, ACCOUNT_LOCKED);
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<HttpResponse> usernameExistException(UsernameAlreadyExistsException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(ActivityTemplateUniqueIDAlreadyExistsException.class)
    public ResponseEntity<HttpResponse> unqiueIDExistException(ActivityTemplateUniqueIDAlreadyExistsException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<HttpResponse> userNotFoundException(UserNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    // this also send the supported method in the error message
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<HttpResponse> methodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        // there should only be one request method per Controller function so supportedMethod is singular
        HttpMethod supportedMethod = Objects.requireNonNull(exception.getSupportedHttpMethods()).iterator().next();
        return createHttpResponse(METHOD_NOT_ALLOWED, String.format(METHOD_IS_NOT_ALLOWED, supportedMethod));
    }

    // the generic exception if all other exceptions do not apply (logger will reveal what happened)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpResponse> internalServerErrorException(Exception exception) {
        log.error(exception.getMessage());
        return createHttpResponse(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG);
    }

    @ExceptionHandler(NoResultException.class)
    public ResponseEntity<HttpResponse> notFoundException(NoResultException exception) {
        log.error(exception.getMessage());
        return createHttpResponse(NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<HttpResponse> missingParamException(MissingServletRequestParameterException exception) {
        return createHttpResponse(UNPROCESSABLE_ENTITY, Objects.requireNonNull(exception.getMessage()));
    }

    @ExceptionHandler(BadJSONBodyException.class)
    public ResponseEntity<HttpResponse> badJSONBodyException(BadJSONBodyException exception) {
        return createHttpResponse(UNPROCESSABLE_ENTITY, Objects.requireNonNull(exception.getMessage()));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<HttpResponse> IO_Exception(IOException exception) {
        log.error(exception.getMessage());
        return createHttpResponse(INTERNAL_SERVER_ERROR, FILE_PROCESSING_ERROR);
    }

    @ExceptionHandler(MissingPersonalDataRequiredException.class)
    public ResponseEntity<HttpResponse> missingData(MissingPersonalDataRequiredException e) {
        return createHttpResponse(UNPROCESSABLE_ENTITY, Objects.requireNonNull(e.getMessage()));
    }

    private ResponseEntity<HttpResponse> createHttpResponse(HttpStatus status, String message) {
        HttpResponse httpResponse = new HttpResponse(
                status.value(), status, status.getReasonPhrase().toUpperCase(), message.toUpperCase(), new Date());

        // body and status only; no headers
        return new ResponseEntity<>(httpResponse, status);
    }
}

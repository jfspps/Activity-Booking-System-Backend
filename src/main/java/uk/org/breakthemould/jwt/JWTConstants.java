package uk.org.breakthemould.jwt;

public class JWTConstants {

    // milliseconds (1 day)
    public static final long EXPIRATION_TIME = 86_400_000;

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String JWT_TOKEN_HEADER = "JWT";

    public static final String TOKEN_CANNOT_BE_VERIFIED = "Token cannot be verified";
    public static final String GET_MY_COMPANY = "Break The Mould";
    public static final String GET_MY_COMPANY_ADMIN = "BTM admin";
    public static final String AUTHORITIES = "authorities";

    public static final String FORBIDDEN_MESSAGE = "Log in to access this resource";
    public static final String ACCESS_DENIED_MESSAGE = "Not permitted to access this resource";

    public static final String OPTIONS_HTTP_METHOD = "OPTIONS";
}

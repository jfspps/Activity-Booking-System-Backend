package uk.org.breakthemould.config;

public class ControllerConstants {

    public static final String ROOT_URL_V1 = "/api/v1";
    public static final String[] PUBLIC_URLS = {
            "/",
            ControllerConstants.ROOT_URL_V1 + "/users/login",
            ControllerConstants.ROOT_URL_V1 + "/users/resetPassword",
            ControllerConstants.ROOT_URL_V1 + "/users",
            "/swagger-ui/*",
            "/v3/api-docs",
            "/v3/api-docs/*"};
}

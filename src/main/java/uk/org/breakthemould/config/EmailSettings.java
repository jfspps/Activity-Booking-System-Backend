package uk.org.breakthemould.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailSettings {

    public static final String SIMPLE_MAIL_TRANSFER_PROTOCOL = "smtps";
    public static final String CC_EMAIL = "";
    public static final String EMAIL_SUBJECT = "Break The Mould - booking system account";
    public static final String SMTP_HOST = "mail.smtp.host";
    public static final String SMTP_AUTH = "mail.smtp.auth";
    public static final String SMTP_PORT = "mail.smtp.port";

    @Value("${email.username}")
    private String username;

    @Value("${email.password}")
    private String password;

    @Value("${email.from}")
    private String from_email;

    @Value("${email.smtp.server}")
    private String email_smtp_server;

    @Value("${email.smtp.port}")
    private int email_smtp_port;

    @Value("${email.smtp.tls.enable}")
    private String email_smtp_tls_enable;

    @Value("${email.smtp.tls.required}")
    private String email_smtp_tls_required;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFrom_email() {
        return from_email;
    }

    public void setFrom_email(String from_email) {
        this.from_email = from_email;
    }

    public String getEmail_smtp_server() {
        return email_smtp_server;
    }

    public void setEmail_smtp_server(String email_smtp_server) {
        this.email_smtp_server = email_smtp_server;
    }

    public int getEmail_smtp_port() {
        return email_smtp_port;
    }

    public void setEmail_smtp_port(int email_smtp_port) {
        this.email_smtp_port = email_smtp_port;
    }

    public String getEmail_smtp_tls_enable() {
        return email_smtp_tls_enable;
    }

    public void setEmail_smtp_tls_enable(String email_smtp_tls_enable) {
        this.email_smtp_tls_enable = email_smtp_tls_enable;
    }

    public String getEmail_smtp_tls_required() {
        return email_smtp_tls_required;
    }

    public void setEmail_smtp_tls_required(String email_smtp_tls_required) {
        this.email_smtp_tls_required = email_smtp_tls_required;
    }
}

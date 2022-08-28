package uk.org.breakthemould.service;

import uk.org.breakthemould.domain.security.LoginFailure;
import uk.org.breakthemould.domain.security.User;

import java.sql.Timestamp;
import java.util.List;

public interface LoginFailureService extends BaseService<LoginFailure, Long> {

    //handle lockout
    List<LoginFailure> findAllByUserAndCreatedDateIsAfter(User user, Timestamp timestamp);
}

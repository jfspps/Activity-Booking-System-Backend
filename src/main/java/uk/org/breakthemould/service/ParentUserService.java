package uk.org.breakthemould.service;

import uk.org.breakthemould.domain.security.ParentUser;

import java.util.Set;

public interface ParentUserService extends BaseService<ParentUser, Long> {

    Set<ParentUser> findAllByParentFirstNameAndLastName(String firstname, String lastname);

    ParentUser saveAndFlush(ParentUser parentUser);
}

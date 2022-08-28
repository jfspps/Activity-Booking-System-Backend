package uk.org.breakthemould.service;

import uk.org.breakthemould.domain.security.Authority;

public interface AuthorityService extends BaseService<Authority, Long> {

    Authority findByPermission(String permission);
}

package uk.org.breakthemould.service.sdjpa.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.org.breakthemould.domain.security.AdminUser;
import uk.org.breakthemould.domain.security.Authority;
import uk.org.breakthemould.exception.domain.AuthorityNotFoundException;
import uk.org.breakthemould.repository.security.AuthorityRepository;
import uk.org.breakthemould.service.AuthorityService;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class AuthoritySDjpaService implements AuthorityService {

    private final AuthorityRepository authorityRepository;

    public AuthoritySDjpaService(AuthorityRepository authorityRepository) {
        this.authorityRepository = authorityRepository;
    }

    @Override
    public Authority save(Authority object) {
        Authority saved = authorityRepository.save(object);
        log.debug("Saved authority: " + saved.getPermission());
        return saved;
    }

    @Override
    public Authority findById(Long aLong) {
        log.debug("Searching for authority with id: " + aLong);
        return authorityRepository.findById(aLong).orElseThrow(
                () -> new AuthorityNotFoundException("Authority with ID supplied not found")
        );
    }

    @Override
    public Set<Authority> findAll() {
        log.debug("Searching for all authorities");
        Set<Authority> authorities = new HashSet<>();
        authorities.addAll(authorityRepository.findAll());
        log.debug("Found " + authorities.size() + " record(s)");
        return authorities;
    }

    @Override
    public void delete(Authority objectT) {
        log.debug("Removing authority from file: " + objectT.getPermission());
        authorityRepository.delete(objectT);
    }

    @Override
    public void deleteById(Long aLong) {
        log.debug("Removing authority with id: " + aLong);
        authorityRepository.deleteById(aLong);
    }

    @Override
    public Authority findByPermission(String permission) {
        log.debug("Searching by permission: " + permission);
        return authorityRepository.findAuthorityByPermission(permission).orElseThrow(
                () -> new AuthorityNotFoundException("Authority with permission supplied not found")
        );
    }
}

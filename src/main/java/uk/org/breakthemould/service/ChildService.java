package uk.org.breakthemould.service;

import uk.org.breakthemould.domain.child.Child;
import uk.org.breakthemould.domain.security.ParentUser;

import java.util.Set;

public interface ChildService extends BaseService<Child, Long> {

    Set<Child> findByParents(ParentUser parentUser);

    Set<Child> findChildrenWithFirstAndLastNamesByParentUsername(String parentUsername, String firstname, String lastname);

    Child findChildWithFirstAndLastNamesByParentUsername(String parentUsername, String firstname, String lastname);
}

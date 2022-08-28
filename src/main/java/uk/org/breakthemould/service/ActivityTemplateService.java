package uk.org.breakthemould.service;

import uk.org.breakthemould.domain.activity.ActivityTemplate;
import uk.org.breakthemould.domain.security.User;

import java.util.Set;

public interface ActivityTemplateService extends BaseService<ActivityTemplate, Long> {

    ActivityTemplate findByUniqueID(String uniqueID);

    Set<ActivityTemplate> findAllByName(String name);

    boolean activityTemplateWithUniqueIDExists(String uniqueID);

    Set<ActivityTemplate> findAllByOwner(User owner);
}

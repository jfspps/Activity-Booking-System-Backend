package uk.org.breakthemould.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import uk.org.breakthemould.domain.DTO.activity.ActivityTemplateDTO;
import uk.org.breakthemould.domain.DTO.activity.ActivityTemplate_put_DTO;
import uk.org.breakthemould.domain.activity.ActivityTemplate;

@Mapper
public interface ActivityTemplateMapper {

    ActivityTemplateMapper INSTANCE = Mappers.getMapper(ActivityTemplateMapper.class);

    ActivityTemplateDTO activityTemplateToActivityTemplateDTO(ActivityTemplate activityTemplate);

    ActivityTemplate_put_DTO activityTemplateToActivityTemplate_put_DTO(ActivityTemplate activityTemplate);
}

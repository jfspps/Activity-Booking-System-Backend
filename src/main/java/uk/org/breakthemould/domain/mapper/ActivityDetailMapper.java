package uk.org.breakthemould.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import uk.org.breakthemould.domain.DTO.activity.ActivityDetailDTO;
import uk.org.breakthemould.domain.DTO.activity.ActivityDetail_new_DTO;
import uk.org.breakthemould.domain.DTO.activity.ActivityDetail_put_DTO;
import uk.org.breakthemould.domain.DTO.activity.ActivityTemplate_put_DTO;
import uk.org.breakthemould.domain.activity.ActivityDetail;

@Mapper
public interface ActivityDetailMapper {
    ActivityDetailMapper INSTANCE = Mappers.getMapper(ActivityDetailMapper.class);

    ActivityDetail_new_DTO activityDetailToActivity_new_DetailDTO(ActivityDetail activityDetail);

    @Mapping(target = "activityTemplate_dto", source = "activityTemplate")
    ActivityDetailDTO activityDetailToActivityDetailDTO(ActivityDetail activityDetail);

    ActivityDetail_put_DTO activityDetailToActivityDetailPUT_DTO(ActivityDetail activityDetail);
}

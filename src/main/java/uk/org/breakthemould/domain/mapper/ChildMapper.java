package uk.org.breakthemould.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import uk.org.breakthemould.domain.DTO.activity.Child_summary_DTO;
import uk.org.breakthemould.domain.DTO.child.ChildDTO;
import uk.org.breakthemould.domain.DTO.personnel.ParentUserDTO;
import uk.org.breakthemould.domain.DTO.personnel.ParentUserDTOList;
import uk.org.breakthemould.domain.child.Child;
import uk.org.breakthemould.domain.security.ParentUser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper
public interface ChildMapper {
    ChildMapper INSTANCE = Mappers.getMapper(ChildMapper.class);

    @Mapping(target = "parents", ignore = true)
    @Mapping(target = "address", source = "addressDTO")
    Child childDTOToChild(ChildDTO childDTO);

    ChildDTO childToChildDTO(Child child);

    Child_summary_DTO childTOChild_summary_DTO(Child child);
}

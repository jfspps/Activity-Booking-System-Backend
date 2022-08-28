package uk.org.breakthemould.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import uk.org.breakthemould.domain.DTO.details.AddressDTO;
import uk.org.breakthemould.domain.personal.Address;

@Mapper
public interface AddressMapper {

    AddressMapper INSTANCE = Mappers.getMapper(AddressMapper.class);

    AddressDTO addressToAddressDTO(Address address);

    @Mapping(target = "staffUsers", ignore = true)
    @Mapping(target = "parentUsers", ignore = true)
    @Mapping(target = "adminUsers", ignore = true)
    Address addressDTOToAddress(AddressDTO addressDTO);
}

package uk.org.breakthemould.service;

import uk.org.breakthemould.domain.personal.Address;

import java.util.Set;

public interface AddressService extends BaseService<Address, Long> {

    // UK post codes are generally not unique!
    Set<Address> findAllByPostCode(String postCode);

    Address findAddressByAllFields(String firstLine, String secondLine, String townCity, String postcode);
}

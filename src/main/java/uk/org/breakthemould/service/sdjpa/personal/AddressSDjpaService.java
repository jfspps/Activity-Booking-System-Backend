package uk.org.breakthemould.service.sdjpa.personal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import uk.org.breakthemould.domain.personal.Address;
import uk.org.breakthemould.repository.personal.AddressRepository;
import uk.org.breakthemould.service.AddressService;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class AddressSDjpaService implements AddressService {

    private final AddressRepository addressRepository;

    public AddressSDjpaService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public Set<Address> findAllByPostCode(String postCode) {
        log.debug("Searching by postcode: " + postCode);
        Set<Address> addresses = new HashSet<>();
        addresses.addAll(addressRepository.findAllByPostCodeContainingIgnoreCase(postCode));
        log.debug("Found " + addresses.size() + " record(s)");
        return addresses;
    }

    @Override
    public Address findAddressByAllFields(String firstLine, String secondLine, String townCity, String postcode) {
        log.debug("Searching for address by all parameters");
        return addressRepository.findByFirstLineAndSecondLineAndTownCityAndPostCodeIgnoreCase(firstLine, secondLine, townCity, postcode).orElse(null);
    }

    @Override
    public Address save(Address object) {
        Address saved = addressRepository.save(object);
        log.debug("Saved address: " + saved);
        return saved;
    }

    @Override
    public Address findById(Long aLong) {
        log.debug("Searching for address by id: " + aLong);
        return addressRepository.findById(aLong).orElseThrow(
                () -> new NotFoundException("Address not found")
        );
    }

    @Override
    public Set<Address> findAll() {
        log.debug("Searching for all addresses");
        Set<Address> addresses = new HashSet<>();
        addresses.addAll(addressRepository.findAll());
        log.debug("Found " + addresses.size() + " record(s)");
        return addresses;
    }

    @Override
    public void delete(Address objectT) {
        log.debug("Deleting address");
        addressRepository.delete(objectT);
    }

    @Override
    public void deleteById(Long aLong) {
        log.debug("Deleting address with id: " + aLong);
        addressRepository.deleteById(aLong);
    }
}

package uk.org.breakthemould.repository.personal;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.org.breakthemould.domain.personal.Address;

import java.util.Optional;
import java.util.Set;

public interface AddressRepository extends JpaRepository<Address, Long> {

    Set<Address> findAllByPostCodeContainingIgnoreCase(String postcode);

    Optional<Address> findByFirstLineAndSecondLineAndTownCityAndPostCodeIgnoreCase(String firstLine, String secondLine, String townCity, String postcode);
}

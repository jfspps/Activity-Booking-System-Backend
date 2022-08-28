package uk.org.breakthemould.controller.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import uk.org.breakthemould.config.ControllerConstants;
import uk.org.breakthemould.domain.DTO.child.ChildDTO;
import uk.org.breakthemould.domain.DTO.child.ChildEditDTO;
import uk.org.breakthemould.domain.DTO.child.ChildEditListDTO;
import uk.org.breakthemould.domain.DTO.child.ChildListDTO;
import uk.org.breakthemould.domain.DTO.details.AddressDTO;
import uk.org.breakthemould.domain.DTO.personnel.*;
import uk.org.breakthemould.domain.child.Child;
import uk.org.breakthemould.domain.mapper.AddressMapper;
import uk.org.breakthemould.domain.mapper.ChildMapper;
import uk.org.breakthemould.domain.personal.Address;
import uk.org.breakthemould.domain.security.ParentUser;
import uk.org.breakthemould.domain.security.User;
import uk.org.breakthemould.exception.domain.BadJSONBodyException;
import uk.org.breakthemould.exception.domain.MissingPersonalDataRequiredException;
import uk.org.breakthemould.exception.domain.NotFoundException;
import uk.org.breakthemould.service.*;

import javax.mail.MessagingException;
import java.util.*;

import static uk.org.breakthemould.bootstrap.EntityConstants.ADMIN_ROLE;
import static uk.org.breakthemould.bootstrap.EntityConstants.STAFF_ROLE;

@Slf4j
@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@ResponseBody
@Tag(name = "parent-controller", description = "Handles all parent-controlled specific routes")
@RequestMapping(path = ControllerConstants.ROOT_URL_V1 + "/users")
public class ParentController {

    private final UserService userService;
    private final AddressService addressService;
    private final ChildService childService;

    private final ParentUserService parentUserService;
    private final StaffUserService staffUserService;
    private final AdminUserService adminUserService;

    private final EmailService emailService;

    /**
     * Submit personal details of a registering parent
     */
    @Operation(summary = "Allows one parent to register their details for the first time. For single parents, returns \"canActivateAccount=true\". PARENT only.")
    @PreAuthorize("hasAuthority('parent.create')")
    @PutMapping("/registerParent")
    public ResponseEntity<ParentRegisteredDTO> registeringParentDetails(@RequestBody RegisteringParentDTO registeringParentDTO)
            throws NotFoundException {
        log.debug("--------- PUT /users/registerParent: (registering) parent registration requested ---------");

        String username = registeringParentDTO.getUsername();
        if (username == null || username.isBlank()) {
            throw new BadJSONBodyException("Missing username");
        }

        User regParent = userService.findByUsername(username);
        if (regParent == null) {
            throw new NotFoundException("Parent with given username not found");
        }

        checkForPersonalDetails(registeringParentDTO);
        ParentUser regParentUser = regParent.getParentUser();

        regParentUser.setIsRegisteringParent(true);
        regParentUser.setFirstName(registeringParentDTO.getFirstName());
        regParentUser.setLastName(registeringParentDTO.getLastName());
        regParentUser.setContactNumber(registeringParentDTO.getContactNumber());
        regParentUser.setEmail(registeringParentDTO.getEmail());
        regParentUser.setIsPartnered(registeringParentDTO.getIsPartnered());
        regParentUser.setPartnerEmail(registeringParentDTO.getPartnerEmail());

        //get the BTM rep details
        User btmRep = regParentUser.getBtmRep();

        String btmRepEmail;
        if (btmRep.getRole().getRoleName().equals(ADMIN_ROLE)) {
            btmRepEmail = btmRep.getAdminUser().getEmail();
        } else {
            btmRepEmail = btmRep.getStaffUser().getEmail();
        }

        AddressDTO addressDTO = registeringParentDTO.getAddressDTO();
        Address address = Address.builder()
                .firstLine(addressDTO.getFirstLine())
                .secondLine(addressDTO.getSecondLine())
                .townCity(addressDTO.getTownCity())
                .postCode(addressDTO.getPostCode())
                .build();
        address.getParentUsers().add(regParentUser);

        addressService.save(address);
        regParentUser.setAddress(address);
        regParentUser.setCanActivateAccount(true);

        parentUserService.save(regParentUser);

        ParentRegisteredDTO parentRegisteredDTO = new ParentRegisteredDTO();
        parentRegisteredDTO.setUsername(username);
        parentRegisteredDTO.setCanActivateAccount(regParentUser.getCanActivateAccount());
        parentRegisteredDTO.setIsRegisteringParent(regParentUser.getIsRegisteringParent());
        parentRegisteredDTO.setIsPartnered(regParentUser.getIsPartnered());
        parentRegisteredDTO.setPartnerEmail(regParentUser.getPartnerEmail());

        if (registeringParentDTO.getPartnerEmail() == null || registeringParentDTO.getPartnerEmail().isBlank()) {
            // no need to confirm other partner's details
            log.debug("API: Partner email not received; other parent records not expected");
            new Thread(new SendBTMRepNewAccount(btmRepEmail, username)).start();

            return new ResponseEntity<>(parentRegisteredDTO, HttpStatus.OK);
        } else {
            // still awaiting other parent data
            log.debug("API: Partner email received; other parent records will be expected");
            new Thread(new SendRegParentOtherParentAccountOngoing(regParentUser.getEmail())).start();
            new Thread(new SendBTMRepNewAccount(btmRepEmail, username)).start();

            return new ResponseEntity<>(parentRegisteredDTO, HttpStatus.OK);
        }
    }

    /**
     * Submit personal details of other non-registering parent
     */
    @Operation(summary = "Allows the other non-registering parent to submit their details for the first time. Returns \"canActivateAccount=true\". PARENT only.")
    @PreAuthorize("hasAuthority('parent.create')")
    @PutMapping("/otherParent")
    public ResponseEntity<ParentRegisteredDTO> nonRegisteringParentDetails(@RequestBody RegisteringParentDTO nonRegisteringParentDTO)
            throws NotFoundException {
        log.debug("--------- PUT /users/otherParent: (other) parent registration requested ---------");

        String username = nonRegisteringParentDTO.getUsername();
        if (username == null || username.isBlank()) {
            throw new BadJSONBodyException("Missing username");
        }

        User nonRegParent = userService.findByUsername(username);
        if (nonRegParent == null) {
            throw new NotFoundException("Parent with given username not found");
        }

        checkForPersonalDetails(nonRegisteringParentDTO);

        ParentUser nonRegParentUser = nonRegParent.getParentUser();

        nonRegParentUser.setIsRegisteringParent(false);
        nonRegParentUser.setFirstName(nonRegisteringParentDTO.getFirstName());
        nonRegParentUser.setLastName(nonRegisteringParentDTO.getLastName());
        nonRegParentUser.setContactNumber(nonRegisteringParentDTO.getContactNumber());
        nonRegParentUser.setEmail(nonRegisteringParentDTO.getEmail());
        nonRegParentUser.setIsPartnered(nonRegisteringParentDTO.getIsPartnered());
        nonRegParentUser.setPartnerEmail(nonRegisteringParentDTO.getPartnerEmail());

        //get the BTM rep details
        User btmRep = nonRegParent.getParentUser().getBtmRep();

        String btmRepEmail;
        if (btmRep.getRole().getRoleName().equals(ADMIN_ROLE)) {
            btmRepEmail = btmRep.getAdminUser().getEmail();
        } else {
            btmRepEmail = btmRep.getStaffUser().getEmail();
        }

        AddressDTO addressDTO = nonRegisteringParentDTO.getAddressDTO();
        Address address = Address.builder()
                .firstLine(addressDTO.getFirstLine())
                .secondLine(addressDTO.getSecondLine())
                .townCity(addressDTO.getTownCity())
                .postCode(addressDTO.getPostCode())
                .build();
        address.getParentUsers().add(nonRegParentUser);

        addressService.save(address);
        nonRegParentUser.setAddress(address);
        nonRegParentUser.setCanActivateAccount(true);
        parentUserService.save(nonRegParentUser);

        ParentRegisteredDTO parentNonRegisteredDTO = new ParentRegisteredDTO();

        // not waiting for more data, so allow completion of registration
        parentNonRegisteredDTO.setUsername(username);
        parentNonRegisteredDTO.setCanActivateAccount(nonRegParentUser.getCanActivateAccount());
        parentNonRegisteredDTO.setIsRegisteringParent(nonRegParentUser.getIsRegisteringParent());
        parentNonRegisteredDTO.setIsPartnered(nonRegParentUser.getIsPartnered());
        parentNonRegisteredDTO.setPartnerEmail(nonRegParentUser.getPartnerEmail());

        new Thread(new SendRegParentActivationEmail(nonRegParentUser.getEmail())).start();
        new Thread(new InformRegParentOtherParentSentDetailsEmail(nonRegParentUser.getPartnerEmail())).start();
        new Thread(new SendBTMRepNewAccount(btmRepEmail, username)).start();

        return new ResponseEntity<>(parentNonRegisteredDTO, HttpStatus.OK);
    }

    /**
     * Get BTM rep details of parent with given username
     */
    @Operation(summary = "Get BTM rep details of parent with given username. Can only be accessed by ADMIN, STAFF and " +
            "PARENT in question.")
    @PreAuthorize("hasAnyAuthority('parent.read', 'staff.read', 'admin.read')")
    @GetMapping("/btmRep")
    public ResponseEntity<ParentUser_btmRep_DTO> getBtmRep(@RequestParam String username) {
        log.debug("--------- GET /users/btmRep: request for BTM rep details received ---------");

        if (!username.equals(getUsername()) && !getRole().equals(ADMIN_ROLE) && !getRole().equals(STAFF_ROLE)) {
            throw new AccessDeniedException("Not permitted to access this information");
        }

        ParentUser parentUser = userService.findByUsername(username).getParentUser();

        ParentUser_btmRep_DTO parentUser_btmRep_dto = new ParentUser_btmRep_DTO();
        parentUser_btmRep_dto.setParentUsername(username);

        User btmRep = userService.findByUsername(parentUser.getBtmRep().getUsername());
        parentUser_btmRep_dto.setBtmRepUsername(btmRep.getUsername());

        if (btmRep.getAdminUser() != null) {
            parentUser_btmRep_dto.setBtmRepFirstName(btmRep.getAdminUser().getFirstName());
            parentUser_btmRep_dto.setBtmRepLastName(btmRep.getAdminUser().getLastName());
        } else {
            parentUser_btmRep_dto.setBtmRepFirstName(btmRep.getStaffUser().getFirstName());
            parentUser_btmRep_dto.setBtmRepLastName(btmRep.getStaffUser().getLastName());
        }

        return new ResponseEntity<>(parentUser_btmRep_dto, HttpStatus.OK);
    }

    /**
     * Update BTM rep details of parent with given username
     */
    @Operation(summary = "Update BTM rep details of parent with given username. ADMIN only.")
    @PreAuthorize("hasAuthority('admin.update')")
    @PutMapping("/btmRep")
    public ResponseEntity<ParentUser_btmRepUsername_DTO> updateBtmRep(
            @RequestBody ParentUser_btmRepUsername_DTO parentUser_btmRepUsername_dto) {
        log.debug("--------- PUT /users/btmRep: change of BTM rep for parent, " + parentUser_btmRepUsername_dto.getParentUsername() + ", requested ---------");
        String parentUsername = parentUser_btmRepUsername_dto.getParentUsername();

        ParentUser parentUser = userService.findByUsername(parentUsername).getParentUser();
        User btmRep = userService.findByUsername(parentUser_btmRepUsername_dto.getBtmRepUsername());

        parentUser.setBtmRep(btmRep);
        if (btmRep.getAdminUser() != null) {
            btmRep.getAdminUser().getParents().add(parentUser);
            adminUserService.save(btmRep.getAdminUser());
        } else {
            btmRep.getStaffUser().getParents().add(parentUser);
            staffUserService.save(btmRep.getStaffUser());
        }

        parentUserService.save(parentUser);

        ParentUser_btmRepUsername_DTO returnDTO = new ParentUser_btmRepUsername_DTO();
        returnDTO.setBtmRepUsername(btmRep.getUsername());
        returnDTO.setParentUsername(parentUsername);
        return new ResponseEntity<>(returnDTO, HttpStatus.OK);
    }

    /**
     * Activate the parent(s)' account, with username and email for verification. Restricted to the BTM rep concerned.
     */
    @Operation(summary = "Activate the parent(s)' account, with username and email for verification. Restricted to the BTM rep (ADMIN or STAFF) concerned.")
    @PreAuthorize("hasAnyAuthority('admin.update', 'staff.update')")
    @PostMapping("/activateParentsAccount")
    public ResponseEntity<ParentsAccountsActivatedDTO> activateParentsAccounts(
            @RequestBody ActivateParentsAccountsDTO activateParentsAccountsDTO) {

        log.debug("--------- POST /users/activateParentsAccount: activation of parent's account requested ---------");
        User regParent = userService.findByUsername(activateParentsAccountsDTO.getRegisteringParentUsername());
        ParentUser regParentUser = regParent.getParentUser();

        // check BTM rep is assigned to registering parent
        if (!regParent.getParentUser().getBtmRep().getUsername().equals(getUsername())) {
            throw new AccessDeniedException("Only the BTM rep assigned can activate this parent's account");
        }

        // get the BTM rep's email
        User btmRep = userService.findByUsername(getUsername());
        String btmRepEmail;
        if (btmRep.getRole().getRoleName().equals(ADMIN_ROLE)) {
            btmRepEmail = btmRep.getAdminUser().getEmail();
        } else
            btmRepEmail = btmRep.getStaffUser().getEmail();

        ParentsAccountsActivatedDTO activatedDTO = new ParentsAccountsActivatedDTO();

        if (activateParentsAccountsDTO.getHasPartner() && !activateParentsAccountsDTO.getOtherParentUsername().isBlank()) {
            // process the data of couples
            log.debug("API: Processing data for a couple");
            User otherParent = userService.findByUsername(activateParentsAccountsDTO.getOtherParentUsername());
            ParentUser otherParentUser = otherParent.getParentUser();

            // check BTM rep is assigned to other parent
            if (!otherParentUser.getBtmRep().getUsername().equals(getUsername())) {
                throw new AccessDeniedException("Only the BTM rep assigned can activate this parent's account");
            }
            log.debug("API: BTM rep assignment for both parents is OK");

            // check that the email addresses are supplied (this forces the user to supply the correct data)
            String regParentEmail = activateParentsAccountsDTO.getGetRegisteringParentEmail();
            String otherParentEmail = activateParentsAccountsDTO.getOtherParentEmail();

            if (regParentEmail == null || regParentEmail.isBlank() || otherParentEmail == null || otherParentEmail.isBlank()) {
                throw new BadJSONBodyException("Missing email addresses needed");
            }

            // check that their own email addresses on file match
            if (!regParentUser.getEmail().equals(regParentEmail)
                    || !otherParentUser.getEmail().equals(otherParentEmail)) {
                throw new MissingPersonalDataRequiredException("Emails supplied do not match those on file");
            }

            // check that their partner's email on file are in line
            if (!regParentUser.getEmail().equals(otherParentUser.getPartnerEmail())
                    || !otherParentUser.getEmail().equals(regParentUser.getEmail())) {
                throw new MissingPersonalDataRequiredException("Email of partner for one or both parents on file do not match");
            }
            log.debug("API: Email address details for both parents are OK");

            // checks done, commit changes
            regParentUser.setPartner(otherParent);
            regParentUser.setCanUploadChildDataMakeBookings(true);
            otherParentUser.setPartner(regParent);
            otherParentUser.setCanUploadChildDataMakeBookings(true);

            parentUserService.save(regParentUser);
            parentUserService.save(otherParentUser);

            activatedDTO.setRegisteringParentUsername(regParentUser.getUser().getUsername());
            activatedDTO.setGetRegisteringParentEmail(regParentEmail);
            activatedDTO.setOtherParentUsername(otherParentUser.getUser().getUsername());
            activatedDTO.setOtherParentEmail(otherParentEmail);
            activatedDTO.setHasPartner(true);
            activatedDTO.setCanUploadChildDataMakeBookings(true);

            // send the emails
            new Thread(new SendAccountActivatedEmail(regParentEmail)).start();
            new Thread(new SendAccountActivatedEmail(otherParentEmail)).start();
            new Thread(new SendBTMrepParentAccountActivated(btmRepEmail, regParent.getUsername(),
                    regParentEmail, otherParent.getUsername(), otherParentEmail)).start();
        } else {
            // process a sole parent application
            log.debug("API: Processing data for a sole parent");
            String regParentEmail = activateParentsAccountsDTO.getGetRegisteringParentEmail();

            if (regParentEmail == null || regParentEmail.isBlank()) {
                throw new BadJSONBodyException("Missing email address needed");
            }

            // check that their email address on file match
            if (!regParentUser.getEmail().equals(regParentEmail)) {
                throw new MissingPersonalDataRequiredException("Email supplied does not match that on file");
            }
            log.debug("API: Email address details are OK");

            // checks done, commit changes
            regParentUser.setCanUploadChildDataMakeBookings(true);

            parentUserService.save(regParentUser);

            activatedDTO.setRegisteringParentUsername(regParentUser.getUser().getUsername());
            activatedDTO.setGetRegisteringParentEmail(regParentEmail);
            activatedDTO.setOtherParentUsername(null);
            activatedDTO.setOtherParentEmail(null);
            activatedDTO.setHasPartner(false);
            activatedDTO.setCanUploadChildDataMakeBookings(true);

            new Thread(new SendAccountActivatedEmail(regParentEmail)).start();
            new Thread(new SendBTMrepParentAccountActivated(btmRepEmail, regParent.getUsername(),
                    regParentEmail, null, null)).start();
        }

        return new ResponseEntity<>(activatedDTO, HttpStatus.OK);
    }

    /**
     * Submit personal details of a child or children
     */
    @Operation(summary = "Allows a parent (with activated account) to upload the personal details of their child/children. PARENT only.")
    @PreAuthorize("hasAuthority('parent.create')")
    @PostMapping("/child")
    public ResponseEntity<ChildListDTO> newChildren(@RequestBody ChildListDTO childListDTO) {
        log.debug("--------- POST /users/child: child/children registration requested ---------");

        ParentUser parentUser = userService.findByUsername(childListDTO.getParentUsername()).getParentUser();

        if (parentUser.getUser().getUsername().equals(getUsername())
                && parentUser.getCanActivateAccount() && parentUser.getCanUploadChildDataMakeBookings()) {

            List<ChildDTO> childListDTOList = childListDTO.getChildDTOs();

            log.debug("API: Checks done, processing child/children records...");
            for (ChildDTO childDTO : childListDTOList) {
                Child child = ChildMapper.INSTANCE.childDTOToChild(childDTO);

                addressService.save(child.getAddress());
                childService.save(child);

                // link with parents
                parentUser.getChildren().add(child);
                parentUserService.save(parentUser);
            }

            return new ResponseEntity<>(childListDTO, HttpStatus.OK);
        } else
            throw new AccessDeniedException("Not authorised to upload details for this child");
    }

    /**
     * Retrieve details of a child or children (children list sorted by last name and then first name)
     */
    @Operation(summary = "Allows a parent to retrieve the personal details of their child/all children. " +
            "A 'children' list is sorted by last name and then first name. ADMIN, STAFF and PARENT (with activated account) responsible only. " +
            "Note that the other parent who is responsible for the same children cannot access this resource and must use their own username.")
    @PreAuthorize("hasAnyAuthority('admin.read', 'staff.read', 'parent.read')")
    @GetMapping("/children")
    public ResponseEntity<ChildListDTO> getChildren(@RequestParam String username) {
        log.debug("--------- GET /users/children: child/children records requested ---------");

        User currentUser = userService.findByUsername(getUsername());

        User parentUser = userService.findByUsername(username);
        if (parentUser == null || parentUser.getUsername() == null || parentUser.getParentUser() == null) {
            throw new BadJSONBodyException("Must pass a parent user as a parameter");
        }

        // let staff and admin users through
        if (currentUser.getParentUser() != null && (!currentUser.getUsername().equals(parentUser.getUsername())
                || !currentUser.getParentUser().getCanActivateAccount()
                || !currentUser.getParentUser().getCanUploadChildDataMakeBookings())){
            throw new AccessDeniedException("Access restricted to parent with given username and with an account that has" +
                    " been activated by their BTM rep");
        }

        log.debug("API: Checks done, searching database for child/children records");
        ChildListDTO childListDTO = new ChildListDTO();
        childListDTO.setParentUsername(parentUser.getUsername());

        if ((currentUser.getParentUser() != null && getUsername().equals(username))
                || currentUser.getStaffUser() != null || currentUser.getAdminUser() != null) {
            List<Child> children = new ArrayList<>(parentUser.getParentUser().getChildren());
            Collections.sort(children);

            for (Child child : children) {
                ChildDTO childDTO = ChildMapper.INSTANCE.childToChildDTO(child);
                childDTO.setAddressDTO(AddressMapper.INSTANCE.addressToAddressDTO(child.getAddress()));
                childListDTO.getChildDTOs().add(childDTO);
            }
        }

        return new ResponseEntity<>(childListDTO, HttpStatus.OK);
    }

    /**
     * Update details of a child or children; the returned list of children will match exactly how they were submitted in the PUT request
     */
    @Operation(summary = "Allows a parent or BTM rep to update the personal details of a child/all children." +
            " BTM rep (ADMIN or STAFF) and PARENT (with activated account) responsible only. The fields newFirstName and newLastName are ignored if empty. " +
            "The returned list of children will match exactly how they were submitted in the PUT request")
    @PreAuthorize("hasAnyAuthority('parent.update', 'admin.update', 'staff.update')")
    @PutMapping("/children")
    public ResponseEntity<ChildListDTO> updateChildren(@RequestBody ChildEditListDTO childListDTO) throws NotFoundException {
        log.debug("--------- PUT /users/children: update to child/children records requested ---------");

        User currentUser = userService.findByUsername(getUsername());
        User parent = userService.findByUsername(childListDTO.getParentUsername());

        if (parent.getParentUser() == null) {
            throw new BadJSONBodyException("Given username is not a parent user");
        }

        ParentUser parentUser = parent.getParentUser();
        User btmRep = parentUser.getBtmRep();

        // restrict child record changes to btm rep and the parent
        if ((currentUser.getStaffUser() != null || currentUser.getAdminUser() != null) &&
                !btmRep.getUsername().equals(getUsername())) {
            throw new AccessDeniedException("Not permitted to edit this data");
        } else if (currentUser.getParentUser() != null && (
                !currentUser.getUsername().equals(childListDTO.getParentUsername())
                        || !currentUser.getParentUser().getCanActivateAccount()
                        || !currentUser.getParentUser().getCanUploadChildDataMakeBookings())) {

            throw new AccessDeniedException("Not permitted to edit this data");
        }

        log.debug("API: Checks done, processing updates...");
        // List use here preserves the order of each child submitted
        List<ChildEditDTO> childDTOS = childListDTO.getChildDTOs();
        Set<Child> children = parent.getParentUser().getChildren();

        ChildListDTO returnList = new ChildListDTO();

        for (ChildEditDTO childDTO : childDTOS) {
            Optional<Child> found = children.stream().filter(child -> child.getFirstName().equals(childDTO.getFirstName()) &&
                    child.getLastName().equals(childDTO.getLastName())).findFirst();

            if (found.isPresent()) {

                Child onFile = found.get();
                log.debug("API: Child found: " + onFile.getFirstName() + " " + onFile.getLastName());

                if (childDTO.getNewFirstName() != null && !childDTO.getNewFirstName().isBlank()){
                    onFile.setFirstName(childDTO.getNewFirstName());
                }
                if (childDTO.getLastName() != null && !childDTO.getNewLastName().isBlank()){
                    onFile.setLastName(childDTO.getNewLastName());
                }

                onFile.setEmail(childDTO.getEmail());
                onFile.setConsentsToEmails(childDTO.getConsentsToEmails());

                AddressDTO addressDTO = childDTO.getAddressDTO();
                Address address = AddressMapper.INSTANCE.addressDTOToAddress(addressDTO);

                Address addressOnFile = addressService.findAddressByAllFields(
                        address.getFirstLine(), address.getSecondLine(), address.getTownCity(), address.getPostCode());
                if (addressOnFile == null) {
                    addressService.save(address);
                    onFile.setAddress(address);
                } else
                    onFile.setAddress(addressOnFile);

                onFile.setSchoolName(childDTO.getSchoolName());
                onFile.setReceivesFreeSchoolMeals(childDTO.getReceivesFreeSchoolMeals());
                onFile.setHasAdditionalNeeds(childDTO.getHasAdditionalNeeds());

                if (onFile.getHasAdditionalNeeds()) {
                    onFile.setAdditionalNeeds(childDTO.getAdditionalNeeds());
                }

                onFile.setHasAllergies(childDTO.getHasAllergies());

                if (onFile.getHasAllergies()) {
                    onFile.setAllergies(childDTO.getAllergies());
                }

                onFile.setConsentsToPhotoVideoStorage(childDTO.getConsentsToPhotoVideoStorage());
                onFile.setEmergencyContactName(childDTO.getEmergencyContactName());
                onFile.setEmergencyContactNumber(childDTO.getEmergencyContactNumber());
                childService.save(onFile);
                ChildDTO returnChildDTO = ChildMapper.INSTANCE.childToChildDTO(onFile);
                returnList.getChildDTOs().add(returnChildDTO);
            } else {
                log.debug("API: Child not found: " + childDTO.getFirstName() + " " + childDTO.getLastName());
                throw new NotFoundException("Child not found");
            }
        }

        returnList.setParentUsername(parent.getUsername());

        String btmRepEmail;
        if (btmRep.getAdminUser() != null) {
            btmRepEmail = btmRep.getAdminUser().getEmail();
        } else
            btmRepEmail = btmRep.getStaffUser().getEmail();

        // send the emails
        if (parentUser.getPartner() != null) {
            User otherParent = parentUser.getPartner();
            new Thread(new SendBTMRepAndParentsChildDataChange(btmRepEmail, parentUser.getEmail(), parent.getUsername(), otherParent.getParentUser().getEmail(), otherParent.getUsername())).start();
        } else
            new Thread(new SendBTMRepAndParentsChildDataChange(btmRepEmail, parentUser.getEmail(), parent.getUsername(), null, null)).start();

        return new ResponseEntity<>(returnList, HttpStatus.OK);
    }

    class SendBTMRepAndParentsChildDataChange implements Runnable {
        String btmRepEmail;
        String regParentEmail;
        String regParentUsername;
        String otherParentEmail;
        String otherParentUsername;

        public SendBTMRepAndParentsChildDataChange(String btmRepEmail, String regParentEmail, String regParentUsername, String otherParentEmail, String otherParentUsername) {
            this.btmRepEmail = btmRepEmail;
            this.regParentEmail = regParentEmail;
            this.regParentUsername = regParentUsername;
            this.otherParentEmail = otherParentEmail;
            this.otherParentUsername = otherParentUsername;
        }

        @Override
        public void run() {
            try {
                emailService.sendBTMrepAndParentsChangesToChildData(btmRepEmail, regParentUsername, regParentEmail, otherParentUsername, otherParentEmail);
            } catch (MessagingException messagingException) {
                log.debug("API: Problem sending emails: " + messagingException.getMessage());
            }
        }
    }

    class SendBTMRepNewAccount implements Runnable {
        String btmRepEmail;
        String newUserUsername;

        SendBTMRepNewAccount(String btmRepEmail, String newUserUsername) {
            this.btmRepEmail = btmRepEmail;
            this.newUserUsername = newUserUsername;
        }

        @Override
        public void run() {
            try {
                emailService.informBTMRepNewAccountEmail(btmRepEmail, newUserUsername);
            } catch (MessagingException messagingException) {
                log.debug("API: Problem sending email: " + messagingException.getMessage());
            }
        }
    }

    class SendRegParentOtherParentAccountOngoing implements Runnable {
        String regParentEmail;

        SendRegParentOtherParentAccountOngoing(String regParentEmail) {
            this.regParentEmail = regParentEmail;
        }

        @Override
        public void run() {
            try {
                emailService.sendRegParentOtherParentAccountOngoing(regParentEmail);
            } catch (MessagingException messagingException) {
                log.debug("API: Problem sending email: " + messagingException.getMessage());
            }
        }
    }

    class SendRegParentActivationEmail implements Runnable {
        String nonRegParentEmail;

        SendRegParentActivationEmail(String nonRegParentEmail) {
            this.nonRegParentEmail = nonRegParentEmail;
        }

        @Override
        public void run() {
            try {
                emailService.sendRegParentActivationEmail(nonRegParentEmail);
            } catch (MessagingException messagingException) {
                log.debug("API: Problem sending email: " + messagingException.getMessage());
            }
        }
    }

    class InformRegParentOtherParentSentDetailsEmail implements Runnable {
        String nonRegParentPartnerEmail;

        InformRegParentOtherParentSentDetailsEmail(String nonRegParentPartnerEmail) {
            this.nonRegParentPartnerEmail = nonRegParentPartnerEmail;
        }

        @Override
        public void run() {
            try {
                emailService.informRegParentOtherParentSentDetailsEmail(nonRegParentPartnerEmail);
            } catch (MessagingException messagingException) {
                log.debug("API: Problem sending email: " + messagingException.getMessage());
            }
        }
    }

    class SendAccountActivatedEmail implements Runnable {
        String email;

        public SendAccountActivatedEmail(String email) {
            this.email = email;
        }

        @Override
        public void run() {
            try {
                emailService.sendAccountActivatedEmail(email);
            } catch (MessagingException messagingException) {
                log.debug("API: Problem sending email: " + messagingException.getMessage());
            }
        }
    }

    class SendBTMrepParentAccountActivated implements Runnable {
        String btmRepEmail;
        String regParentUsername;
        String regParentEmail;
        String otherParentUsername;
        String otherParentEmail;

        SendBTMrepParentAccountActivated(String btmRepEmail, String regParentUsername, String regParentEmail,
                                         String otherParentUsername, String otherParentEmail) {
            this.btmRepEmail = btmRepEmail;
            this.regParentUsername = regParentUsername;
            this.regParentEmail = regParentEmail;
            this.otherParentUsername = otherParentUsername;
            this.otherParentEmail = otherParentEmail;
        }

        @Override
        public void run() {
            try {
                emailService.sendBTMrepParentAccountActivated(btmRepEmail, regParentUsername, regParentEmail, otherParentUsername, otherParentEmail);
            } catch (MessagingException messagingException) {
                log.debug("API: Problem sending email: " + messagingException.getMessage());
            }
        }
    }

    private void checkForPersonalDetails(RegisteringParentDTO registeringParentDTO) {
        if (registeringParentDTO.getFirstName() == null
                || registeringParentDTO.getFirstName().isBlank()
                || registeringParentDTO.getLastName() == null
                || registeringParentDTO.getLastName().isBlank()
                || registeringParentDTO.getAddressDTO() == null
                || registeringParentDTO.getContactNumber() == null
                || registeringParentDTO.getContactNumber().isBlank()
                || registeringParentDTO.getEmail() == null
                || registeringParentDTO.getEmail().isBlank()
        ) {
            throw new MissingPersonalDataRequiredException("Not all required fields submitted");
        }
        log.debug("API: Personal details submitted are sufficient");
    }

    /**
     * Retrieves the username of the authenticated user
     */
    private String getUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal != null) {
            return principal.toString();
        } else {
            return null;
        }
    }

    /**
     * Retrieves the role of the authenticated user
     */
    private String getRole() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal != null) {
            User user = userService.findByUsername(principal.toString());
            return user.getRole().getRoleName();
        } else {
            return null;
        }
    }
}

package uk.org.breakthemould.controller.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import uk.org.breakthemould.config.ControllerConstants;
import uk.org.breakthemould.domain.DTO.child.PersonRemovedDTO;
import uk.org.breakthemould.domain.DTO.details.AddressDTO;
import uk.org.breakthemould.domain.DTO.personnel.*;
import uk.org.breakthemould.domain.activity.ActivityDetail;
import uk.org.breakthemould.domain.activity.Booking;
import uk.org.breakthemould.domain.child.Child;
import uk.org.breakthemould.domain.mapper.*;
import uk.org.breakthemould.domain.personal.Address;
import uk.org.breakthemould.domain.security.*;
import uk.org.breakthemould.exception.domain.BadJSONBodyException;
import uk.org.breakthemould.exception.domain.UsernameAlreadyExistsException;
import uk.org.breakthemould.service.*;

import javax.mail.MessagingException;
import javax.websocket.server.PathParam;

import java.util.*;

import static uk.org.breakthemould.bootstrap.EntityConstants.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@ResponseBody
@Tag(name = "user-controller", description = "Handles all user related routes")
@RequestMapping(path = ControllerConstants.ROOT_URL_V1 + "/users")
public class AccountController {

    private final UserService userService;
    private final RoleService roleService;
    private final AddressService addressService;

    private final ParentUserService parentUserService;
    private final StaffUserService staffUserService;
    private final AdminUserService adminUserService;
    private final ActivityDetailService activityDetailService;
    private final BookingService bookingService;
    private final ChildService childService;

    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Get a list of all users on file; sorts list by username
     */
    @Operation(summary = "Retrieve a list of all users on file; sorts list by username; STAFF and ADMIN only")
    @PreAuthorize("hasAnyAuthority('admin.read', 'staff.read')")
    @GetMapping("/")
    public ResponseEntity<UserDTOList> getAllUsers(){
        log.debug("--------- GET /users: list of all users requested ---------");

        List<User> users = new ArrayList<>(userService.findAll());
        Collections.sort(users);

        UserDTOList userDTOList = new UserDTOList();

        users.forEach(user -> userDTOList.getUserDTOList().add(UserMapper.INSTANCE.userToUserDTO(user)));

        return new ResponseEntity<>(userDTOList, HttpStatus.OK);
    }

    @Operation(summary = "Get a list of all ADMIN users on file. ADMIN and STAFF only.")
    @PreAuthorize("hasAnyAuthority('admin.read', 'staff.read')")
    @GetMapping("/admin")
    public ResponseEntity<AdminUserDTOList> getAdminUsers() {
        log.debug("--------- GET /users/admin: list of admin users requested ---------");

        List<User> users = new ArrayList<>(userService.findAll());
        Collections.sort(users);

        AdminUserDTOList adminUserDTOList = new AdminUserDTOList();

        users.forEach(user -> {
            if (user.getAdminUser() != null){
                adminUserDTOList.getUserDTOList().add(AdminUserMapper.INSTANCE.adminUserToAdminUserDTO(user.getAdminUser()));
            }
        });

        return new ResponseEntity<>(adminUserDTOList, HttpStatus.OK);
    }

    /**
     * Register new admin account with username and email address
     */
    @Operation(summary = "Register new admin account with username and email address. Tip: use an email " +
            "address you can access and check the inbox. ADMIN only.")
    @PreAuthorize("hasAuthority('admin.create')")
    @PostMapping("/admin")
    public ResponseEntity<AdminUserDTO> newAdminAccount(@RequestBody NewAdminStaffUserDTO newAdminStaffUserDTO) {
        log.debug("--------- POST /users/admin: new administrator account requested ---------");
        String username = newAdminStaffUserDTO.getUsername();
        String email = newAdminStaffUserDTO.getEmail();

        if (userService.checkUsernameExists(username)){
            throw new UsernameAlreadyExistsException("Username: " + username + " already in use");
        }

        String password = generatePassword();

        // todo: remove these two printouts at release
        log.warn("---Please remove this from the release version-----");
        log.debug("API: New admin username: " + username + " with new password: " + password);

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(roleService.findByRoleName(ADMIN_ROLE))
                .build();

        AdminUser adminUser = AdminUser.builder()
                .email(email)
                .user(user)
                .build();

        user.setAdminUser(adminUser);

        AdminUserDTO adminUserDTO = AdminUserMapper.INSTANCE.adminUserToAdminUserDTO(adminUserService.save(adminUser));
        adminUserDTO.setUserDTO(UserMapper.INSTANCE.userToUserDTO(user));

        new Thread(new SendNewUsernameEmail(username, email)).start();
        new Thread(new SendNewPasswordEmail(password, email)).start();

        return new ResponseEntity<>(adminUserDTO, HttpStatus.OK);
    }

    @Operation(summary = "Get a list of all STAFF users on file. ADMIN and STAFF only.")
    @PreAuthorize("hasAnyAuthority('admin.read', 'staff.read')")
    @GetMapping("/staff")
    public ResponseEntity<StaffUserDTOList> getStaffUsers() {
        log.debug("--------- GET /users/staff: list of staff users requested ---------");

        List<User> users = new ArrayList<>(userService.findAll());
        Collections.sort(users);

        StaffUserDTOList staffUserDTOList = new StaffUserDTOList();

        users.forEach(user -> {
            if (user.getStaffUser() != null){
                staffUserDTOList.getUserDTOList().add(StaffUserMapper.INSTANCE.staffUserToStaffUserDTO(user.getStaffUser()));
            }
        });

        return new ResponseEntity<>(staffUserDTOList, HttpStatus.OK);
    }

    /**
     * Register new staff account with username and email address
     */
    @Operation(summary = "Register new staff account with username and email address. Tip: use an email address you " +
            "can access and check the inbox. ADMIN only.")
    @PostMapping("/staff")
    @PreAuthorize("hasAuthority('admin.create')")
    public ResponseEntity<StaffUserDTO> newStaffAccount(@RequestBody NewAdminStaffUserDTO newAdminStaffUserDTO) {
        log.debug("--------- POST /users/staff: new staff account requested ---------");
        String username = newAdminStaffUserDTO.getUsername();
        String email = newAdminStaffUserDTO.getEmail();

        if (userService.checkUsernameExists(username)){
            throw new UsernameAlreadyExistsException("Username: " + username + " already in use");
        }

        String password = generatePassword();

        // todo: remove these at release
        log.warn("---Please remove this from the release version-----");
        log.debug("API: New staff username: " + username + " with new password: " + password);

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(roleService.findByRoleName(STAFF_ROLE))
                .build();

        StaffUser staffUser = StaffUser.builder()
                .email(email)
                .user(user)
                .build();

        user.setStaffUser(staffUser);

        StaffUserDTO staffUserDTO = StaffUserMapper.INSTANCE.staffUserToStaffUserDTO(staffUserService.save(staffUser));
        staffUserDTO.setUserDTO(UserMapper.INSTANCE.userToUserDTO(user));

        new Thread(new SendNewUsernameEmail(username, email)).start();
        new Thread(new SendNewPasswordEmail(password, email)).start();

        return new ResponseEntity<>(staffUserDTO, HttpStatus.OK);
    }

    @Operation(summary = "Get a list of all PARENT users on file. ADMIN and STAFF only.")
    @PreAuthorize("hasAnyAuthority('admin.read', 'staff.read')")
    @GetMapping("/parent")
    public ResponseEntity<ParentUserDTOList> getParentUsers() {
        log.debug("--------- GET /users/parent: list of parent users requested ---------");

        List<User> users = new ArrayList<>(userService.findAll());
        Collections.sort(users);

        ParentUserDTOList parentUserDTOList = new ParentUserDTOList();

        users.forEach(user -> {
            if (user.getParentUser() != null){
                parentUserDTOList.getUserDTOList().add(ParentUserMapper.INSTANCE.parentUserToParentUserDTO(user.getParentUser()));
            }
        });

        return new ResponseEntity<>(parentUserDTOList, HttpStatus.OK);
    }

    /**
     * Register new parent account with username and email address
     */
    @Operation(summary = "Register new parent account with username and email address. Tip: use an email address " +
            "you can access and check the inbox. STAFF and ADMIN only.")
    @PostMapping("/parent")
    @PreAuthorize("hasAnyAuthority('admin.create', 'staff.create')")
    public ResponseEntity<ParentUserDTO> newParentAccount(@RequestBody NewParentUserDTO newParentUserDTO) {

        log.debug("--------- POST /users/parent: new parent account requested ---------");
        String username = newParentUserDTO.getUsername();
        String email = newParentUserDTO.getEmail();

        if (userService.checkUsernameExists(username)){
            throw new UsernameAlreadyExistsException("Username: " + username + " already in use");
        }

        String password = generatePassword();

        // todo: remove these at release
        log.warn("---Please remove this from the release version-----");
        log.debug("API: New parent username: " + username + " with new password: " + password);

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(roleService.findByRoleName(PARENT_ROLE))
                .build();

        ParentUser parentUser = ParentUser.builder()
                .email(email)
                .isRegisteringParent(newParentUserDTO.getIsRegisteringParent())
                .user(user)
                .build();

        user.setParentUser(parentUser);

        // need to flush before passing to setBTMRep()
        parentUserService.saveAndFlush(parentUser);

        // assign the new parent to the current admin/staff user, and vice versa
        setBTMrep(parentUser);

        ParentUserDTO parentUserDTO = ParentUserMapper.INSTANCE.parentUserToParentUserDTO(parentUserService.save(parentUser));
        parentUserDTO.setUserDTO(UserMapper.INSTANCE.userToUserDTO(user));

        new Thread(new SendNewUsernameEmail(username, email)).start();
        new Thread(new SendNewPasswordEmail(password, email)).start();

        return new ResponseEntity<>(parentUserDTO, HttpStatus.OK);
    }

    /**
     * Retrieve user's details by database ID
     */
    @Operation(summary = "Retrieve user's comprehensive details by database ID. Route is restricted to a PARENT with the same" +
            " username given or any STAFF/ADMIN.")
    @GetMapping("/byID")
    @PreAuthorize("hasAnyAuthority('admin.read', 'staff.read', 'parent.read')")
    public ResponseEntity<?> getUserById(@RequestParam("userID") String id) throws AccessDeniedException {
        log.debug("--------- GET /users/byID: user with id " + id + " requested ---------");

        User found = userService.findById(Long.valueOf(id));

        if (found.getUsername().equals(getUsername()) || !getRole().equals(PARENT_ROLE)) {
            if (found.getAdminUser() != null){
                AdminUserDTO adminUserDTO = AdminUserMapper.INSTANCE.adminUserToAdminUserDTO(found.getAdminUser());
                adminUserDTO.setAddress(AddressMapper.INSTANCE.addressToAddressDTO(found.getAdminUser().getAddress()));
                adminUserDTO.setUserDTO(UserMapper.INSTANCE.userToUserDTO(found));

                return new ResponseEntity<>(adminUserDTO, HttpStatus.OK);
            } else if (found.getStaffUser() != null){
                StaffUserDTO staffUserDTO = StaffUserMapper.INSTANCE.staffUserToStaffUserDTO(found.getStaffUser());
                staffUserDTO.setAddress(AddressMapper.INSTANCE.addressToAddressDTO(found.getStaffUser().getAddress()));
                staffUserDTO.setUserDTO(UserMapper.INSTANCE.userToUserDTO(found));

                return new ResponseEntity<>(staffUserDTO, HttpStatus.OK);
            } else {
                ParentUser thisParent = found.getParentUser();
                ParentUserDTO parentUserDTO = ParentUserMapper.INSTANCE.parentUserToParentUserDTO(thisParent);
                parentUserDTO.setAddress(AddressMapper.INSTANCE.addressToAddressDTO(thisParent.getAddress()));
                parentUserDTO.setUserDTO(UserMapper.INSTANCE.userToUserDTO(found));
                parentUserDTO.setCanUploadChildDataMakeBookings(thisParent.getCanUploadChildDataMakeBookings());
                parentUserDTO.setCanActivateAccount(thisParent.getCanActivateAccount());
                parentUserDTO.setIsRegisteringParent(thisParent.getIsRegisteringParent());
                parentUserDTO.setPartnerEmail(thisParent.getPartnerEmail());

                return new ResponseEntity<>(parentUserDTO, HttpStatus.OK);
            }
        } else
            throw new AccessDeniedException("You are not permitted to access this resource");
    }

    /**
     * Retrieve user's details by username
     */
    @Operation(summary = "Retrieve user's details summary by username. Route is restricted to a PARENT with the same" +
            " username given or any STAFF/ADMIN.")
    @GetMapping("/{username}/username")
    @PreAuthorize("hasAnyAuthority('admin.read', 'staff.read', 'parent.read')")
    public ResponseEntity<?> getUserByUsername(@PathVariable("username") String username) throws AccessDeniedException {
        log.debug("--------- GET /users/" + username + ": user with username " + username + " requested ---------");

        User found = userService.findByUsername(username);

        if (found.getUsername().equals(getUsername()) || !getRole().equals(PARENT_ROLE)) {
            AdminStaffParent_edit_DTO userDTO;

            if (found.getAdminUser() != null){
                userDTO = AdminStaffParent_edit_Mapper.INSTANCE.adminUserMapping(found.getAdminUser());
                userDTO.setUserDTO(UserMapper.INSTANCE.userToUserDTO(found));
                userDTO.setAddress(AddressMapper.INSTANCE.addressToAddressDTO(found.getAdminUser().getAddress()));

                return new ResponseEntity<>(userDTO, HttpStatus.OK);
            } else if (found.getStaffUser() != null){
                userDTO = AdminStaffParent_edit_Mapper.INSTANCE.staffUserMapping(found.getStaffUser());
                userDTO.setUserDTO(UserMapper.INSTANCE.userToUserDTO(found));
                userDTO.setAddress(AddressMapper.INSTANCE.addressToAddressDTO(found.getStaffUser().getAddress()));

                return new ResponseEntity<>(userDTO, HttpStatus.OK);
            } else {
                ParentUser thisParent = found.getParentUser();
                ParentUserDTO parentUserDTO = ParentUserMapper.INSTANCE.parentUserToParentUserDTO(thisParent);
                parentUserDTO.setAddress(AddressMapper.INSTANCE.addressToAddressDTO(thisParent.getAddress()));
                parentUserDTO.setUserDTO(UserMapper.INSTANCE.userToUserDTO(found));
                parentUserDTO.setCanUploadChildDataMakeBookings(thisParent.getCanUploadChildDataMakeBookings());
                parentUserDTO.setCanActivateAccount(thisParent.getCanActivateAccount());
                parentUserDTO.setIsRegisteringParent(thisParent.getIsRegisteringParent());
                parentUserDTO.setPartnerEmail(thisParent.getPartnerEmail());

                return new ResponseEntity<>(parentUserDTO, HttpStatus.OK);
            }

        } else
            throw new AccessDeniedException("You are not permitted to access this resource");
    }

    /**
     * Update a user's details (exc. password) by username
     */
    @Operation(summary = "Update a user's personal details (exc. password) by username. Route is restricted to a PARENT with the same" +
            " username given or any STAFF/ADMIN. Virtually all fields under \"account\" are ignored.")
    @PutMapping("/{username}/username")
    @PreAuthorize("hasAnyAuthority('admin.update', 'staff.update', 'parent.update')")
    public ResponseEntity<?> updateUserByUsername(@PathVariable("username") String username,
                                                                 @RequestBody AdminStaffParent_edit_DTO adminStaffParentEditDto)
            throws AccessDeniedException {

        log.debug("--------- PUT /users/" + username + ": updates to user with username " + username + " requested ---------");

        User found = userService.findByUsername(username);
        Address oldAddress;
        Address savedAddress;
        String updatedEmail = adminStaffParentEditDto.getEmail();

        if (found.getUsername().equals(getUsername()) || !getRole().equals(PARENT_ROLE)) {
            if (found.getAdminUser() != null){
                log.debug("API: Admin user recognised");

                AdminUser foundAdmin = found.getAdminUser();
                foundAdmin.setEmail(updatedEmail);
                foundAdmin.setContactNumber(adminStaffParentEditDto.getContactNumber());
                foundAdmin.setFirstName(adminStaffParentEditDto.getFirstName());
                foundAdmin.setLastName(adminStaffParentEditDto.getLastName());

                oldAddress = foundAdmin.getAddress();

                // address may be null, particularly for Admin or Staff
                Address address = new Address();
                AddressDTO addressOnFile = adminStaffParentEditDto.getAddress();
                savedAddress = checkAndSetAddress(address, addressOnFile);

                if (oldAddress != null){
                    oldAddress.getAdminUsers().remove(foundAdmin);
                    // todo: need to check if oldAddress can be deleted if empty
                    addressService.save(oldAddress);
                }

                foundAdmin.setAddress(savedAddress);

                adminUserService.save(foundAdmin);
                savedAddress.getAdminUsers().add(foundAdmin);

                addressService.save(savedAddress);
                userService.save(found);

                new Thread(new SendUpdatePersonalDetailsEmail(foundAdmin.getEmail())).start();

                AdminUserDTO adminUserDTO = AdminUserMapper.INSTANCE.adminUserToAdminUserDTO(foundAdmin);
                adminUserDTO.setAddress(AddressMapper.INSTANCE.addressToAddressDTO(savedAddress));
                adminUserDTO.setUserDTO(UserMapper.INSTANCE.userToUserDTO(found));

                return new ResponseEntity<>(adminUserDTO, HttpStatus.OK);
            } else if (found.getStaffUser() != null){
                log.debug("API: Staff user recognised");

                StaffUser foundStaff = found.getStaffUser();
                foundStaff.setEmail(updatedEmail);
                foundStaff.setContactNumber(adminStaffParentEditDto.getContactNumber());
                foundStaff.setFirstName(adminStaffParentEditDto.getFirstName());
                foundStaff.setLastName(adminStaffParentEditDto.getLastName());

                oldAddress = foundStaff.getAddress();

                // address may be null, particularly for Admin or Staff
                Address address = new Address();
                AddressDTO addressOnFile = adminStaffParentEditDto.getAddress();
                savedAddress = checkAndSetAddress(address, addressOnFile);

                if (oldAddress != null){
                    oldAddress.getAdminUsers().remove(foundStaff);
                    // todo: need to check if oldAddress can be deleted if empty
                    addressService.save(oldAddress);
                }

                foundStaff.setAddress(savedAddress);

                staffUserService.save(foundStaff);
                savedAddress.getStaffUsers().add(foundStaff);

                addressService.save(savedAddress);
                userService.save(found);

                new Thread(new SendUpdatePersonalDetailsEmail(foundStaff.getEmail())).start();

                StaffUserDTO staffUserDTO = StaffUserMapper.INSTANCE.staffUserToStaffUserDTO(foundStaff);
                staffUserDTO.setAddress(AddressMapper.INSTANCE.addressToAddressDTO(savedAddress));
                staffUserDTO.setUserDTO(UserMapper.INSTANCE.userToUserDTO(found));

                return new ResponseEntity<>(staffUserDTO, HttpStatus.OK);
            } else {
                log.debug("API: Parent user recognised");

                ParentUser foundParent = found.getParentUser();
                foundParent.setEmail(updatedEmail);
                foundParent.setContactNumber(adminStaffParentEditDto.getContactNumber());
                foundParent.setFirstName(adminStaffParentEditDto.getFirstName());
                foundParent.setLastName(adminStaffParentEditDto.getLastName());

                oldAddress = foundParent.getAddress();

                // address may be null, particularly for Admin or Staff
                Address address = new Address();
                AddressDTO addressOnFile = adminStaffParentEditDto.getAddress();
                savedAddress = checkAndSetAddress(address, addressOnFile);

                if (oldAddress != null){
                    oldAddress.getAdminUsers().remove(foundParent);
                    // todo: need to check if oldAddress can be deleted if empty
                    addressService.save(oldAddress);
                }

                foundParent.setAddress(savedAddress);

                parentUserService.save(foundParent);
                savedAddress.getParentUsers().add(foundParent);

                addressService.save(savedAddress);
                userService.save(found);

                new Thread(new SendUpdatePersonalDetailsEmail(foundParent.getEmail())).start();

                ParentUserDTO parentUserDTO = ParentUserMapper.INSTANCE.parentUserToParentUserDTO(foundParent);
                parentUserDTO.setAddress(AddressMapper.INSTANCE.addressToAddressDTO(savedAddress));
                parentUserDTO.setUserDTO(UserMapper.INSTANCE.userToUserDTO(found));
                parentUserDTO.setCanActivateAccount(foundParent.getCanActivateAccount());
                parentUserDTO.setCanUploadChildDataMakeBookings(foundParent.getCanUploadChildDataMakeBookings());
                parentUserDTO.setIsRegisteringParent(foundParent.getIsRegisteringParent());
                parentUserDTO.setPartnerEmail(foundParent.getPartnerEmail());

                return new ResponseEntity<>(parentUserDTO, HttpStatus.OK);
            }
        } else
            throw new AccessDeniedException("You are not permitted to access this resource");
    }

    /**
     * Lock/unlock a user's account
     */
    @Operation(summary = "Lock/unlock a user's account. Restricted to ADMIN users to lock ADMIN/STAFF/PARENT accounts, and then also" +
            " restricted to STAFF user who lock/unlock PARENT accounts.")
    @PutMapping("/toggleLock")
    @PreAuthorize("hasAnyAuthority('admin.update', 'staff.update')")
    public ResponseEntity<UserLockUnlockDTO> lockUnlockAccount(@RequestBody UserLockUnlockDTO userLockUnlockDTO){
        log.debug("--------- PUT /users/toggleLock: lock/unlock account requested ---------");

        User currentUser = userService.findByUsername(getUsername());
        User userTohandle = userService.findByUsername(userLockUnlockDTO.getUsername());

        if (currentUser.getAdminUser() != null
                && (userTohandle.getAdminUser() != null || userTohandle.getStaffUser() != null || userTohandle.getParentUser() != null)
                && !userTohandle.getUsername().equals(getUsername())){
            log.debug("API: Admin user attempting to lock/unlock staff or parent account recognised");
            // note, accounts are nonLocked by default
            userTohandle.setAccountNonLocked(!userLockUnlockDTO.getAccountIsLocked());
            userService.save(userTohandle);
        } else if (currentUser.getStaffUser() != null && userTohandle.getParentUser() != null){
            log.debug("API: Staff user attempting to lock/unlock parent account recognised");
            // note, accounts are nonLocked by default
            userTohandle.setAccountNonLocked(!userLockUnlockDTO.getAccountIsLocked());
            userService.save(userTohandle);
        } else
            throw new AccessDeniedException("Not permitted to lock/unlock this user's account");

        return new ResponseEntity<>(userLockUnlockDTO, HttpStatus.OK);
    }

    /**
     * Deletes a child's details from file. Must be performed before removing the parents' records.
     * Restricted to BTM rep (ADMIN or STAFF) of this child's parent only.
     */
    @Operation(summary = "Deletes a child's details from file. Must be performed before removing the parents' records." +
            " Restricted to BTM rep (ADMIN or STAFF) of this child's parent only.")
    @DeleteMapping("/child")
    @PreAuthorize("hasAnyAuthority('admin.delete', 'staff.delete')")
    public ResponseEntity<PersonRemovedDTO> removeChild(@RequestBody ParentChildDTO parentChildDTO){
        log.debug("--------- DELETE /child: child record removal requested ---------");

        User parent = userService.findByUsername(parentChildDTO.getParentUsername());

        if (parent.getParentUser() == null){
            throw new BadJSONBodyException("Username does not pertain to parent user");
        }

        // prevent other BTM reps from deleting this account
        User BTMRep = parent.getParentUser().getBtmRep();
        if (!BTMRep.getUsername().equals(getUsername())){
            throw new AccessDeniedException("Not permitted to delete this record");
        }

        Set<Child> children = parent.getParentUser().getChildren();
        Optional<Child> found = children.stream().filter(child -> child.getFirstName().equals(parentChildDTO.getChildFirstName())
                && child.getLastName().equals(parentChildDTO.getChildLastName())).findFirst();

        if (found.isPresent()){
            log.debug("API: Found child record. Processing related records...");
            Child childToRemove = found.get();
            // go through bookings
            if (!childToRemove.getBookings().isEmpty()){
                log.debug("API: Bookings found, processing...");
                Set<Booking> bookings = childToRemove.getBookings();
                bookings.forEach(booking -> {
                    booking.getChildrenTakingPart().remove(childToRemove);
                    log.debug("API: Removed booking with ref: " + booking.getBookingRef());
                    ActivityDetail bookingActivityDetail = booking.getActivityDetail();
                    if (childToRemove.getReceivesFreeSchoolMeals()){
                        bookingActivityDetail.setFreeMealPlacesTaken(bookingActivityDetail.getFreeMealPlacesTaken() - 1);
                        log.debug("API: Free school meal places taken updated to: " + bookingActivityDetail.getFreeMealPlacesTaken());
                    } else {
                        bookingActivityDetail.setNonFreeMealPlacesTaken(bookingActivityDetail.getNonFreeMealPlacesTaken() - 1);
                        log.debug("API: Non-free school meal places taken updated to: " + bookingActivityDetail.getNonFreeMealPlacesTaken());
                    }
                    activityDetailService.save(bookingActivityDetail);
                    bookingService.save(booking);
                });
            } else
                log.debug("No activity bookings found for this child");

            // remove both parents and vice versa
            Set<ParentUser> parents = childToRemove.getParents();
            for (ParentUser parentOnFile : parents) {
                parentOnFile.getChildren().remove(childToRemove);
                log.debug("API: Removed child record from parent: " + parentOnFile.getFirstName() + " " + parentOnFile.getLastName());
                parentUserService.save(parentOnFile);
            }
            log.debug("API: Parent records updated");

            childToRemove.setParents(null);
            childToRemove.setAddress(null);
            childService.delete(childToRemove);
        }

        PersonRemovedDTO personRemovedDTO = new PersonRemovedDTO();
        personRemovedDTO.setMessage("The child, " + parentChildDTO.getChildFirstName() + " " + parentChildDTO.getChildLastName() +
                ", has been removed from the database");
        return new ResponseEntity<>(personRemovedDTO, HttpStatus.OK);
    }

    /**
     * Deletes a parent's details from file. Can be performed only after removing related child records.
     * Restricted to BTM rep (ADMIN or STAFF) of this parent only.
     */
    @Operation(summary = "Deletes a parent's details from file. Can be performed only after removing related child records." +
            " Restricted to BTM rep (ADMIN or STAFF) of this parent only.")
    @DeleteMapping("/parent")
    @PreAuthorize("hasAnyAuthority('admin.delete', 'staff.delete')")
    public ResponseEntity<PersonRemovedDTO> removeParent(@RequestBody User_usernameOnly_DTO usernameOnly_dto){
        log.debug("--------- DELETE /parent: parent account removal requested ---------");

        User parent = userService.findByUsername(usernameOnly_dto.getUsername());

        if (parent.getParentUser() == null){
            throw new BadJSONBodyException("Username does not pertain to parent user");
        }

        // prevent other BTM reps from deleting this account
        User BTMRep = parent.getParentUser().getBtmRep();
        if (!BTMRep.getUsername().equals(getUsername())){
            throw new AccessDeniedException("Not permitted to delete this record");
        }

        ParentUser parentUser = parent.getParentUser();

        // check all child records have been removed
        if (!parentUser.getChildren().isEmpty()){
            throw new AccessDeniedException("Please remove all related child records first");
        }

        log.debug("API: Checks done, processing request...");

        Address parentAddress = parentUser.getAddress();
        parentAddress.getParentUsers().remove(parentUser);
        addressService.save(parentAddress);

        log.debug("API: BTM rep records next...");

        if (BTMRep.getRole().equals(ADMIN_ROLE)){
            BTMRep.getAdminUser().getParents().remove(parentUser);
            log.debug("API: Removed parent from BTM rep, : " + BTMRep.getAdminUser().getFirstName() + " " + BTMRep.getAdminUser().getLastName());
            adminUserService.save(BTMRep.getAdminUser());
        } else {
            BTMRep.getStaffUser().getParents().remove(parentUser);
            log.debug("API: Removed parent from BTM rep, " + BTMRep.getStaffUser().getFirstName() + " " + BTMRep.getStaffUser().getLastName());
            staffUserService.save(BTMRep.getStaffUser());
        }

        log.debug("API: Partner details next...");

        if (parentUser.getPartner() != null && parentUser.getPartner().getParentUser() != null){
            ParentUser partner = parentUser.getPartner().getParentUser();
            log.debug("API: Parent has a partner: " + partner.getFirstName() + " " + partner.getLastName());
            partner.setPartner(null);
            log.debug("API: Updated Partner's record");
            parentUserService.save(partner);
            parentUser.setPartner(null);
        } else
            log.debug("API: Parent not partnered to anyone on file");

        log.debug("API: Activity detail bookings next...");

        if (!parentUser.getBookings().isEmpty()){
            log.debug("API: Bookings found, processing...");
            Set<Booking> bookings = parentUser.getBookings();
            bookings.forEach(booking -> {
                booking.getParentsTakingPart().remove(parentUser);
                log.debug("API: Removed booking with reference: " + booking.getBookingRef());
                bookingService.save(booking);
            });
        } else
            log.debug("API: No bookings found");

        parent.setParentUser(null);
        parentUserService.delete(parentUser);

        Role role = parent.getRole();
        role.getUsers().remove(parent);
        roleService.save(role);
        parent.setRole(null);

        userService.delete(parent);
        log.debug("API: Parent user record deleted and all related security entities updated");

        PersonRemovedDTO personRemovedDTO = new PersonRemovedDTO();
        personRemovedDTO.setMessage("The parent with username: " + usernameOnly_dto.getUsername() +
                ", has been removed from the database");
        return new ResponseEntity<>(personRemovedDTO, HttpStatus.OK);
    }

    class SendNewUsernameEmail implements Runnable {
        String username;
        String email;

        SendNewUsernameEmail(String username, String email) {
            this.username = username;
            this.email = email;
        }

        @Override
        public void run() {
            try {
                emailService.sendNewUsernameEmail(username, email);
            } catch (MessagingException e){
                log.debug("API: Problem sending email: " + e.getMessage());
            }
        }
    }

    class SendNewPasswordEmail implements Runnable {
        String password;
        String email;

        SendNewPasswordEmail(String password, String email) {
            this.password = password;
            this.email = email;
        }

        @Override
        public void run() {
            try {
                emailService.sendNewPasswordEmail(password, email);
            } catch (MessagingException e){
                log.debug("API: Problem sending email: " + e.getMessage());
            }
        }
    }

    class SendUpdatePersonalDetailsEmail implements Runnable {
        String email;

        SendUpdatePersonalDetailsEmail(String email) {
            this.email = email;
        }

        @Override
        public void run() {
            try {
                emailService.sendUpdatePersonalDetailsEmail(email);
            } catch (MessagingException e){
                log.debug("API: Problem sending email: " + e.getMessage());
            }
        }
    }

    private void setBTMrep(ParentUser newParentUser) {
        if (getRole().equals(ADMIN_ROLE)){
            AdminUser adminUser = userService.findByUsername(getUsername()).getAdminUser();
            newParentUser.setBtmRep(adminUser.getUser());

            log.debug("API: Assigning BTM representative: " + adminUser.getUser().getUsername());
            adminUser.getParents().add(newParentUser);
            adminUserService.save(adminUser);

        } else if (getRole().equals(STAFF_ROLE)){
            StaffUser staffUser = userService.findByUsername(getUsername()).getStaffUser();
            newParentUser.setBtmRep(staffUser.getUser());

            log.debug("API: Assigning BTM representative: " + staffUser.getUser().getUsername());
            staffUser.getParents().add(newParentUser);
            staffUserService.save(staffUser);
        }
    }

    /**
     * Checks if address already exists and if so returns it. Otherwise saves a new address and returns saved address.
     */
    private Address checkAndSetAddress(Address address, AddressDTO addressOnFile) {
        Address onFile = addressService.findAddressByAllFields(
                addressOnFile.getFirstLine(),
                addressOnFile.getSecondLine(),
                addressOnFile.getTownCity(),
                addressOnFile.getPostCode());

        if (onFile == null){
            Address savedAddress;
            address.setFirstLine(addressOnFile.getFirstLine());
            address.setSecondLine(addressOnFile.getSecondLine());
            address.setTownCity(addressOnFile.getTownCity());
            address.setPostCode(addressOnFile.getPostCode());
            savedAddress = addressService.save(address);
            return savedAddress;
        }
        return onFile;
    }

    private String generatePassword() {
        log.debug("API: Generating password");
        // random password of ten characters
        return RandomStringUtils.randomAlphanumeric(10);
    }

    /**
     * Retrieves the username of the authenticated user
     */
    private String getUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal != null){
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
        if (principal != null){
            User user = userService.findByUsername(principal.toString());
            return user.getRole().getRoleName();
        } else {
            return null;
        }
    }
}

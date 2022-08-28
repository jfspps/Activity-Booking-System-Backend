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
import org.webjars.NotFoundException;
import uk.org.breakthemould.config.ControllerConstants;
import uk.org.breakthemould.domain.DTO.activity.*;
import uk.org.breakthemould.domain.DTO.details.AddressDTO;
import uk.org.breakthemould.domain.DTO.personnel.*;
import uk.org.breakthemould.domain.activity.ActivityDetail;
import uk.org.breakthemould.domain.activity.ActivityDetailComparator;
import uk.org.breakthemould.domain.activity.ActivityTemplate;
import uk.org.breakthemould.domain.activity.Booking;
import uk.org.breakthemould.domain.child.Child;
import uk.org.breakthemould.domain.mapper.*;
import uk.org.breakthemould.domain.personal.Address;
import uk.org.breakthemould.domain.security.AdminUser;
import uk.org.breakthemould.domain.security.ParentUser;
import uk.org.breakthemould.domain.security.StaffUser;
import uk.org.breakthemould.domain.security.User;
import uk.org.breakthemould.exception.domain.BadJSONBodyException;
import uk.org.breakthemould.exception.domain.DateTimeException;
import uk.org.breakthemould.service.*;

import javax.mail.MessagingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static uk.org.breakthemould.bootstrap.EntityConstants.ADMIN_ROLE;

@Slf4j
@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@ResponseBody
@Tag(name = "activity-detail-controller", description = "Handles all activity detail related routes")
@RequestMapping(path = ControllerConstants.ROOT_URL_V1 + "/activityDetail")
public class ActivityDetailController {

    private final ActivityTemplateService activityTemplateService;
    private final ActivityDetailService activityDetailService;
    private final UserService userService;
    private final AddressService addressService;
    private final ChildService childService;
    private final ParentUserService parentUserService;
    private final BookingService bookingService;
    private final EmailService emailService;

    /**
     * Get a list of all activity details on file; list sorted by organiser's username and then by activity template uniqueID
     */
    @Operation(summary = "Retrieve a list of all activity details on file; list sorted by organiser's username and then by activity template uniqueID; STAFF and ADMIN only")
    @PreAuthorize("hasAnyAuthority('admin.read', 'staff.read')")
    @GetMapping("/")
    public ResponseEntity<ActivityDetailDTOList> getAllActivityDetails(){
        log.debug("--------- GET /activityDetail: list of activity details requested ---------");

        List<ActivityDetail> details = new ArrayList<>(activityDetailService.findAll());
        Collections.sort(details);

        ActivityDetailDTOList activityTemplateDTOList = new ActivityDetailDTOList();

        details.forEach(detail -> activityTemplateDTOList.getActivityDetailDTOs().add(ActivityDetailMapper.INSTANCE.activityDetailToActivityDetailDTO(detail)));

        return new ResponseEntity<>(activityTemplateDTOList, HttpStatus.OK);
    }

    /**
     * Get a list of all activity details for a given organiser (username); list sorted (by organiser's username and then) by activity template uniqueID
     */
    @Operation(summary = "Retrieve a list of all activity details for a given organiser username; list sorted by organiser's username and then by activity template uniqueID. STAFF and ADMIN only.")
    @PreAuthorize("hasAnyAuthority('admin.read', 'staff.read')")
    @GetMapping("/{username}/organiserUsername")
    public ResponseEntity<ActivityDetailDTOList> getAllActivityDetailsByOrganiserUsername(@PathVariable("username") String username){
        log.debug("--------- GET /activityDetail/" + username + "/organiserUsername: list of activity details for organiser, " + username + ", requested ---------");

        List<ActivityDetail> details = new ArrayList<>(activityDetailService.findByOrganiser(userService.findByUsername(username)));
        Collections.sort(details);

        ActivityDetailDTOList activityTemplateDTOList = new ActivityDetailDTOList();

        details.forEach(detail -> activityTemplateDTOList.getActivityDetailDTOs().add(ActivityDetailMapper.INSTANCE.activityDetailToActivityDetailDTO(detail)));

        return new ResponseEntity<>(activityTemplateDTOList, HttpStatus.OK);
    }

    /**
     * Get a list of all activity details for a given activity template uniqueID; list sorted by organiser's username (and then by activity template uniqueID)
     */
    @Operation(summary = "Retrieve a list of all activity details for a given activity template uniqueID; list sorted by organiser's username (and then by activity template uniqueID). STAFF and ADMIN only.")
    @PreAuthorize("hasAnyAuthority('admin.read', 'staff.read')")
    @GetMapping("/{uniqueID}/uniqueID")
    public ResponseEntity<ActivityDetailDTOList> getAllActivityDetailsByUnqiueID(@PathVariable("uniqueID") String uniqueID){
        log.debug("--------- GET /activityDetail/" + uniqueID + "/uniqueID: list of activity details for activity template with uniqueID, " + uniqueID + ", requested ---------");

        ActivityTemplate activityTemplate = activityTemplateService.findByUniqueID(uniqueID);
        List<ActivityDetail> details = new ArrayList<>(activityDetailService.findByActivityTemplate(activityTemplate));
        Collections.sort(details, new ActivityDetailComparator());

        ActivityDetailDTOList activityTemplateDTOList = new ActivityDetailDTOList();

        details.forEach(detail -> activityTemplateDTOList.getActivityDetailDTOs().add(ActivityDetailMapper.INSTANCE.activityDetailToActivityDetailDTO(detail)));

        return new ResponseEntity<>(activityTemplateDTOList, HttpStatus.OK);
    }

    /**
     * Get a list of all activity details that a parent (with username provided) is booked for; list sorted by organiser's username and then by activity template uniqueID
     */
    @Operation(summary = "Retrieve a list of all activity details that a given parent is taking part in; list sorted by organiser's username and then by activity template uniqueID. STAFF and ADMIN only.")
    @PreAuthorize("hasAnyAuthority('admin.read', 'staff.read')")
    @GetMapping("/{username}/parentUsername")
    public ResponseEntity<ActivityDetailDTOList> getAllActivityDetailsByParentUsername(@PathVariable("username") String parentUserName){
        log.debug("--------- GET /activityDetail/" + parentUserName + "/parentUsername: activity details for parent with username, " + parentUserName + ", requested ---------");

        User parent = userService.findByUsername(parentUserName);
        if (parent.getParentUser() == null){
            throw new BadJSONBodyException("Username provided is not a parent");
        }

        ActivityDetailDTOList activityDetailDTOList = new ActivityDetailDTOList();

        List<ActivityDetail> details = new ArrayList<>(activityDetailService.findAll());
        Collections.sort(details);

        details.forEach(activityDetail -> {
            Set<Booking> bookings = activityDetail.getBookings();
            for (Booking booking : bookings){
                if (booking.getParentsTakingPart().contains(parent.getParentUser())){
                    activityDetailDTOList.getActivityDetailDTOs().add(ActivityDetailMapper.INSTANCE.activityDetailToActivityDetailDTO(activityDetail));
                    // no need to look up other family bookings in current activityDetail
                    break;
                }
            }
        });

        return new ResponseEntity<>(activityDetailDTOList, HttpStatus.OK);
    }

    /**
     * Get a list of all activity details that a child (with first and last names, and parent username provided) is booked for; list sorted by organiser's username and then by activity template uniqueID
     */
    @Operation(summary = "Get a list of all activity details that a child (with first and last names, and parent username provided) is booked for;" +
            " list sorted by organiser's username and then by activty template uniqueID. STAFF and ADMIN only.")
    @PreAuthorize("hasAnyAuthority('admin.read', 'staff.read')")
    @GetMapping("{parentUsername}/{firstName}/{lastName}/child")
    public ResponseEntity<ActivityDetailDTOList> getAllActivityDetailsByChildFirstLastNames(@PathVariable("firstName") String firstName,
                                                                                            @PathVariable("lastName") String lastName,
                                                                                            @PathVariable("parentUsername") String parentUsername){
        log.debug("--------- GET /activityDetail/" + parentUsername + "/" + firstName + "/" + lastName + ": activity detail list for given path parameters requested ---------");
        if (!userService.checkUsernameExists(parentUsername)){
            throw new NotFoundException("Parent with username provided not found");
        }

        Child child = childService.findChildWithFirstAndLastNamesByParentUsername(parentUsername, firstName, lastName);

        ActivityDetailDTOList activityDetailDTOList = new ActivityDetailDTOList();

        List<ActivityDetail> details = new ArrayList<>(activityDetailService.findAll());
        Collections.sort(details);

        details.forEach(activityDetail -> {
            Set<Booking> bookings = activityDetail.getBookings();
            for (Booking booking : bookings){
                if (booking.getChildrenTakingPart().contains(child)){
                    activityDetailDTOList.getActivityDetailDTOs().add(ActivityDetailMapper.INSTANCE.activityDetailToActivityDetailDTO(activityDetail));
                    // no need to look up other family bookings in current activityDetail
                    break;
                }
            }
        });

        return new ResponseEntity<>(activityDetailDTOList, HttpStatus.OK);
    }

    /**
     * Retrieve an activity detail
     */
    @Operation(summary = "Retrieve an activity detail by database ID. Accessible to ADMIN, STAFF and PARENT (with activated account).")
    @PreAuthorize("hasAnyAuthority('admin.read', 'staff.read', 'parent.read')")
    @GetMapping("/{detailId}/database")
    public ResponseEntity<ActivityDetailDTO> getActivityDetailByDetailId(@PathVariable("detailId") String detailID) {
        log.debug("--------- GET /activityDetail/" + detailID + "/database: activity detail with id " + detailID + " requested ---------");

        verifyParentUserAccess();

        ActivityDetail activityDetail = activityDetailService.findById(Long.valueOf(detailID));

        ActivityDetailDTO activityDetailDTO = ActivityDetailMapper.INSTANCE.activityDetailToActivityDetailDTO(activityDetail);

        return new ResponseEntity<>(activityDetailDTO, HttpStatus.OK);
    }

    /**
     * Retrieve a list of participants for a given activity template uniqueID and activity detail; bookings are listed by booking reference
     */
    @Operation(summary = "Retrieve a list of participants for a given activity template uniqueID and activity detail date/time (use format 2021-08-01T12:39). Bookings are listed by booking reference. STAFF and ADMIN only.")
    @PreAuthorize("hasAnyAuthority('admin.read', 'staff.read')")
    @GetMapping("/participantList/{uniqueID}/{organiserUsername}")
    public ResponseEntity<ParticipantsDTO> getActivityDetailBookings(@PathVariable("uniqueID") String uniqueID,
                                                                     @PathVariable("organiserUsername") String organiserUsername,
                                                                     @RequestParam("startDateTime") String startMeetingDateTime) throws ParseException {
        log.debug("--------- GET /activityDetail/participantList/" + uniqueID + "/" + organiserUsername + ": activity detail with given path parameters requested ---------");

        if (!userService.checkUsernameExists(organiserUsername)){
            throw new BadJSONBodyException("Organiser username not on file");
        }

        // 2021-08-01T12:39
        log.debug("API: dateTime received: " + startMeetingDateTime);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        Date date = dateFormat.parse(startMeetingDateTime);

        ActivityDetail detailFound = activityDetailService.findByUniqueIDAndMeetingDateTimeAndOrganiser(
                uniqueID, date, userService.findByUsername(organiserUsername));

        ActivityTemplate templateFound = detailFound.getActivityTemplate();

        log.debug("API: Checks done, retrieving records...");
        ParticipantsDTO participantsDTO = new ParticipantsDTO();
        participantsDTO.setActivityTemplate_dto(ActivityTemplateMapper.INSTANCE.activityTemplateToActivityTemplate_put_DTO(templateFound));
        participantsDTO.setId(detailFound.getId());
        participantsDTO.setFreeMealPlacesLimit(detailFound.getFreeMealPlacesLimit());
        participantsDTO.setFreeMealPlacesTaken(detailFound.getFreeMealPlacesTaken());
        participantsDTO.setNonFreeMealPlacesLimit(detailFound.getNonFreeMealPlacesLimit());
        participantsDTO.setNonFreeMealPlacesTaken(detailFound.getNonFreeMealPlacesTaken());
        participantsDTO.setMeetingPlace(AddressMapper.INSTANCE.addressToAddressDTO(detailFound.getMeetingPlace()));
        participantsDTO.setMeetingDateTime(detailFound.getMeetingDateTime());

        User_usernameOnly_DTO usernameOnly_dto = new User_usernameOnly_DTO();
        usernameOnly_dto.setUsername(detailFound.getOrganiser().getUsername());
        participantsDTO.setOrganiser(usernameOnly_dto);

        participantsDTO.setOtherSupervisors(detailFound.getOtherSupervisors());

        FamilyDTOList familyDTOList = new FamilyDTOList();
        participantsDTO.setFamilyDTOList(familyDTOList);

        List<Booking> bookings = new ArrayList<>(detailFound.getBookings());

        if (!bookings.isEmpty()){
            log.debug("API: Bookings found");
            Collections.sort(bookings);

            bookings.forEach(booking -> {
                // each booking is specific to a family
                Set<ParentUser> parentUsers = booking.getParentsTakingPart();
                Parent_summary_DTOList parent_summary_dtoList = new Parent_summary_DTOList();
                parentUsers.forEach(parentUser -> {
                    Parent_summary_DTO parent_summary_dto = ParentUserMapper.INSTANCE.parentUserToParent_summary_DTO(parentUser);
                    parent_summary_dto.setParentUsername(parentUser.getUser().getUsername());
                    parent_summary_dto.setIsTakingPart(true);
                    parent_summary_dtoList.getParentSummaryDTOs().add(parent_summary_dto);
                });

                Set<Child> children = booking.getChildrenTakingPart();
                Child_summary_DTOList child_summary_dtoList = new Child_summary_DTOList();
                children.forEach(child -> {
                    Child_summary_DTO child_summary_dto = ChildMapper.INSTANCE.childTOChild_summary_DTO(child);
                    child_summary_dto.setIsTakingPart(true);
                    child_summary_dtoList.getChildSummaryDTOs().add(child_summary_dto);
                });

                FamilyDTO familyDTO = new FamilyDTO();
                familyDTO.setParent_summary_dtoList(parent_summary_dtoList);
                familyDTO.setChildSummaryDtoList(child_summary_dtoList);
                familyDTOList.getFamilyDTOs().add(familyDTO);
            });
            participantsDTO.setFamilyDTOList(familyDTOList);
        } else
            log.debug("API: No bookings found");

        return new ResponseEntity<>(participantsDTO, HttpStatus.OK);
    }

    /**
     * Submit a new activity detail
     */
    @Operation(summary = "Submit a new activity details. STAFF and ADMIN only.")
    @PreAuthorize("hasAnyAuthority('admin.create', 'staff.create')")
    @PostMapping("/")
    public ResponseEntity<ActivityDetailDTO> newActivityDetail(@RequestBody ActivityDetail_new_DTO activityDetail_new_dto) {
        log.debug("--------- POST /activityDetail: new activity detail submitted ---------");

        // get the template first
        ActivityTemplate_nameUniqueID_DTO activityTemplateDTO = activityDetail_new_dto.getActivityTemplate();
        ActivityTemplate activityTemplate = activityTemplateService.findByUniqueID(activityTemplateDTO.getUniqueID());

        log.debug("API: Activity template found with unique ID: " + activityTemplate.getUniqueID());

        AddressDTO meetingPlaceDTO = activityDetail_new_dto.getMeetingPlace();
        Address meetingPlace = addressService.save(Address.builder()
                .firstLine(meetingPlaceDTO.getFirstLine())
                .secondLine(meetingPlaceDTO.getSecondLine())
                .townCity(meetingPlaceDTO.getTownCity())
                .postCode(meetingPlaceDTO.getPostCode())
                .build());

        log.debug("API: Meeting place processed");

        User organiser = userService.findByUsername(activityDetail_new_dto.getOrganiser().getUsername());
        Date meetingDateTime = activityDetail_new_dto.getMeetingDateTime();

        log.debug("API: Organiser and meeting date/time processed");

        ActivityDetail activityDetail = activityDetailService.save(
                ActivityDetail.builder().activityTemplate(activityTemplate)
                        .meetingPlace(meetingPlace)
                        .meetingTime(meetingDateTime)
                        .meetingDateTime(meetingDateTime)
                        .meetingDate(meetingDateTime)
                        .otherSupervisors(activityDetail_new_dto.getOtherSupervisors())
                        .freeMealPlacesLimit(activityDetail_new_dto.getFreeMealPlacesLimit())
                        .nonFreeMealPlacesLimit(activityDetail_new_dto.getNonFreeMealPlacesLimit())
                        .organiser(organiser)
                        .activityTemplate(activityTemplate)
                        .build());

        ActivityDetailDTO activityDetailDTO = ActivityDetailMapper.INSTANCE.activityDetailToActivityDetailDTO(activityDetail);

        return new ResponseEntity<>(activityDetailDTO, HttpStatus.OK);
    }

    /**
     * Update an activity detail's meeting arrangements and supervisors involved, using the given ID. Restricted to activity organiser.
     */
    @Operation(summary = "Update an activity detail's meeting arrangements and supervisors involved, using the given ID. STAFF and ADMIN only.")
    @PreAuthorize("hasAnyAuthority('admin.update', 'staff.update')")
    @PutMapping("/")
    public ResponseEntity<ActivityDetailDTO> updateActivityDetailByDetailId(@RequestBody ActivityDetail_put_DTO activityDetailDTO) {
        log.debug("--------- PUT /activityDetail: updates to activity detail requested ---------");

        Long activityDetailID = activityDetailDTO.getId();

        ActivityDetail found = activityDetailService.findById(activityDetailID);

        log.debug("API: Found activity detail with id: " + activityDetailID);

        if (!found.getOrganiser().getUsername().equals(getUsername())) {
            throw new AccessDeniedException("Only activity detail organiser may edit this");
        }

        if (activityDetailDTO.getFreeMealPlacesLimit() >= found.getFreeMealPlacesTaken()){
            found.setFreeMealPlacesLimit(activityDetailDTO.getFreeMealPlacesLimit());
            log.debug("API: Free school meal places updated");
        } else
            log.debug("API: FSM participants already booked above given new capacity");

        if (activityDetailDTO.getNonFreeMealPlacesLimit() >= found.getNonFreeMealPlacesTaken()){
            found.setNonFreeMealPlacesLimit(activityDetailDTO.getNonFreeMealPlacesLimit());
            log.debug("API: Non-free school meal places updated");
        } else
            log.debug("API: Non-FSM participants already booked above given new capacity");

        // don't forget to update all three date vars
        found.setMeetingDateTime(activityDetailDTO.getMeetingDateTime());
        found.setMeetingDate(activityDetailDTO.getMeetingDateTime());
        found.setMeetingTime(activityDetailDTO.getMeetingDateTime());

        AddressDTO addressDTO = activityDetailDTO.getMeetingPlace();
        Address newMeetingPlace = addressService.save(Address.builder()
                .firstLine(addressDTO.getFirstLine())
                .secondLine(addressDTO.getSecondLine())
                .townCity(addressDTO.getTownCity())
                .postCode(addressDTO.getPostCode())
                .build());
        found.setMeetingPlace(newMeetingPlace);
        found.setOtherSupervisors(activityDetailDTO.getOtherSupervisors());

        ActivityDetail saved = activityDetailService.save(found);

        ActivityDetailDTO updatedDTO = ActivityDetailMapper.INSTANCE.activityDetailToActivityDetailDTO(saved);
        return new ResponseEntity<>(updatedDTO, HttpStatus.OK);
    }

    /**
     * Find all activity details between startDate and endDate; activities are listed in chronological order
     */
    @Operation(summary = "Find all activity details between startDate and endDate (use format 2021-08-01T12:39); activities listed in chronological order. Accessible" +
            " to ADMIN, STAFF and PARENT (with activated account).")
    @PreAuthorize("hasAnyAuthority('admin.read', 'staff.read', 'parent.read')")
    @GetMapping("/between")
    public ResponseEntity<ActivityDetailDTOList> getActivitiesBetweenDates(@RequestParam("startDateTime") String startDateTimeStr,
                                                                           @RequestParam("endDateTime") String endDateTimeStr) throws ParseException, DateTimeException {
        log.debug("--------- GET /activityDetail/between: activity details between given date/times requested ---------");

        verifyParentUserAccess();

        Set<ActivityDetail> activityDetails = new HashSet<>();

        //check formatting 2021-08-01T12:39
        log.debug("API: start dateTime received: " + startDateTimeStr);
        log.debug("API: end dateTime received: " + endDateTimeStr);

        DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        Date startDateTime = dateTimeFormat.parse(startDateTimeStr);
        Date endDateTime = dateTimeFormat.parse(endDateTimeStr);

        // check sequence
        if (startDateTime.after(endDateTime)) {
            throw new DateTimeException("Start-date must occur before end-date");
        }

        log.debug("API: Date and time checks done");

        activityDetails.addAll(activityDetailService.findByDateAndTimeBetween(startDateTime,
                endDateTime));

        ActivityDetailDTOList activityDetailDTOList = new ActivityDetailDTOList();

        for (ActivityDetail activityDetail : activityDetails) {
            activityDetailDTOList.getActivityDetailDTOs().add(
                    ActivityDetailMapper.INSTANCE.activityDetailToActivityDetailDTO(activityDetail));
        }

        return new ResponseEntity<>(activityDetailDTOList, HttpStatus.OK);
    }

    /**
     * Build a list of activity details with booking references where possible, for the current parent logged in; returned list is sorted by organiser username and then by activity template uniqueID
     */
    @Operation(summary = "Build a list of activities with booking references between date and time (use format 2021-08-01T12:39)." +
            " List is sorted by organiser username and then by activity template uniqueID. Intended for PARENT only (with activated account) for their calendar.")
    @PreAuthorize("hasAnyAuthority('parent.read')")
    @GetMapping("/withBookings")
    public ResponseEntity<BookingDTOList> getActivitiesAndBookingsBetweenDates(
            @RequestParam("startDateTime") String startDateTimeStr, @RequestParam("endDateTime") String endDateTimeStr) throws ParseException, DateTimeException {

        log.debug("--------- GET /activityDetail/withBookings: activity detail list for parent user, " + getUsername() + ", requested ---------");

        verifyParentUserAccess();

        BookingDTOList bookingDTOList = new BookingDTOList();

        //check formatting 2021-08-01T12:39
        log.debug("API: start dateTime received: " + startDateTimeStr);
        log.debug("API: end dateTime received: " + endDateTimeStr);

        DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        Date startDateTime = dateTimeFormat.parse(startDateTimeStr);
        Date endDateTime = dateTimeFormat.parse(endDateTimeStr);

        // check sequence
        if (startDateTime.after(endDateTime)) {
            throw new DateTimeException("Start-date must occur before end-date");
        }

        log.debug("API: Date and time checks done");

        ParentUser currentParent = userService.findByUsername(getUsername()).getParentUser();
        User otherParent_User = currentParent.getPartner();
        ParentUser otherParent = null;
        if (otherParent_User != null) {
            otherParent = otherParent_User.getParentUser();
        }

        Set<Child> currentParentChildren = currentParent.getChildren();  // could be empty

        // get ALL activities between the dateTimes
        Set<ActivityDetail> activityDetails =
                activityDetailService.findByDateAndTimeBetween(startDateTime, endDateTime);

        printOutBookings(bookingDTOList, currentParent, otherParent, currentParentChildren, activityDetails);

        return new ResponseEntity<>(bookingDTOList, HttpStatus.OK);
    }


    /**
     * Update bookings for the parent logged in. Note that this assumes that the frontend will only send a booking in the JSON array
     * for which there are tick/check marks selected for an activity in the calendar. Such activities will receive a booking reference.
     * The returned list order is not changed and follows exactly how the list was submitted with the PUT request.
     */
    @Operation(summary = "Update activity bookings. PARENT only (with activated account). Note that the backend assumes that the frontend will only send a booking in the JSON array" +
            " for which there are tick/check marks selected for an activity in the calendar. Such activities will receive a booking reference. " +
            "The returned list order is not changed and follows exactly how the list was submitted with the PUT request.")
    @PreAuthorize("hasAnyAuthority('parent.update')")
    @PutMapping("/withBookings")
    public ResponseEntity<BookingReplyDTOList> updateBookings(@RequestBody Booking_put_DTOList bookingPutDtoList) {
        log.debug("--------- PUT /activityDetail/withBookings: updates to booking(s) for parent, " + getUsername() + ", requested ---------");

        verifyParentUserAccess();

        // if bookingRef is set, then retrieve and update
        List<Booking_put_DTO> bookings = bookingPutDtoList.getBookings();
        boolean someOneTakingPart = false;

        BookingReplyDTOList bookingReplyDTOList = new BookingReplyDTOList();
        String regParentEmail = null;
        String otherParentEmail = null;

        for (Booking_put_DTO bookingDTO : bookings) {
            // retrieve the family data from bookDTO
            Family_put_DTO familyPutDto = bookingDTO.getFamilyDTO();
            List<Parent_summary_username_DTO> parentSummaryDTOs = familyPutDto.getParent_summary_dtoList().getParentSummaryUsernameDTOs();
            List<Child_summary_put_DTO> childSummaryDTOs = familyPutDto.getChildSummaryDtoList().getChildSummaryDTOs();
            ActivityDetail activityDetail = activityDetailService.findById(bookingDTO.getActivityDetailID());
            ParentUser currentParent = userService.findByUsername(getUsername()).getParentUser();
            Set<Child> childSet = childService.findByParents(currentParent);

            if (childSet.isEmpty()){
                log.debug("API: Cannot process booking(s) for a family with no children on file.");
                throw new NotFoundException("No registered children found on file. Aborting request.");
            }

            log.debug("API: Processing activity detail of a template with uniqueID: " + activityDetail.getActivityTemplate().getUniqueID());

            // needed for return DTO
            List<Parent_summary_DTO> parentSummaryDtoList_return = new ArrayList<>();
            List<Child_summary_DTO> childSummaryDtoList_return = new ArrayList<>();

            Booking booking = new Booking();
            booking.setActivityDetail(activityDetail);

            // look for an existing booking ref, if provided
            if (bookingDTO.getBookingRef() != null && !bookingDTO.getBookingRef().isBlank()
                    && bookingService.findByBookingReference(bookingDTO.getBookingRef()) != null){

                booking = bookingService.findByBookingReference(bookingDTO.getBookingRef());
                log.debug("API: Booking reference found: " + bookingDTO.getBookingRef());
            }

            int FMSplaces = activityDetail.getFreeMealPlacesLimit();
            int nonFSMplaces = activityDetail.getNonFreeMealPlacesLimit();

            // determine what would happen if this booking was applied...
            for (Child_summary_put_DTO childDTO : childSummaryDTOs) {
                Optional<Child> childFound = childSet.stream()
                        .filter(child -> childDTO.getFirstName().equals(child.getFirstName())
                                && childDTO.getLastName().equals(child.getLastName()))
                        .findFirst();
                if (childFound.isPresent() && childDTO.getIsTakingPart()) {
                    if (childFound.get().getReceivesFreeSchoolMeals()) {
                        FMSplaces--;
                    } else {
                        nonFSMplaces--;
                    }
                }
            }

            // ...and check that there are enough places for all children of the family for ALL requests made
            if (FMSplaces < 0 || nonFSMplaces < 0) {
                log.debug("API: Not enough places remaining");
                continue;
            }

            // prerequisites are satisfied, begin processing the booking
            synchronized (activityDetailService.findById(bookingDTO.getActivityDetailID())) {
                log.debug("API: Sufficient places remaining. Temporarily suspending other requests until this transaction is completed...");

                log.debug("API: Looking for child booking records...");
                for (Child_summary_put_DTO childDTO : childSummaryDTOs) {
                    Optional<Child> childFound = childSet.stream()
                            .filter(child -> childDTO.getFirstName().equals(child.getFirstName())
                                    && childDTO.getLastName().equals(child.getLastName()))
                            .findFirst();

                    if (childFound.isPresent()) {
                        log.debug("API: Child, " + childFound.get().getFirstName() + " " + childFound.get().getLastName() + ", taking part: " + childDTO.getIsTakingPart());
                        if (childDTO.getIsTakingPart() && !booking.getChildrenTakingPart().contains(childFound.get())) {
                            log.debug("API: Adding child to booking");
                            someOneTakingPart = true;
                            childFound.get().getBookings().add(booking);
                            booking.getChildrenTakingPart().add(childFound.get());
                            bookingService.save(booking);
                            childService.save(childFound.get());

                            if (childFound.get().getReceivesFreeSchoolMeals()){
                                activityDetail.setFreeMealPlacesTaken(activityDetail.getFreeMealPlacesTaken() + 1);
                                log.debug("API: Child taken one free school meal place");
                            } else {
                                activityDetail.setNonFreeMealPlacesTaken(activityDetail.getNonFreeMealPlacesTaken() + 1);
                                log.debug("API: Child taken one non-free school meal place");
                            }
                        } else if (!childDTO.getIsTakingPart() && booking.getChildrenTakingPart().contains(childFound.get())) {
                            log.debug("API: Removing child from booking");
                            // free up the spaces
                            if (childFound.get().getReceivesFreeSchoolMeals()){
                                activityDetail.setFreeMealPlacesTaken(activityDetail.getFreeMealPlacesTaken() - 1);
                                log.debug("API: An extra free school meal place is now available");
                            } else {
                                activityDetail.setNonFreeMealPlacesTaken(activityDetail.getNonFreeMealPlacesTaken() - 1);
                                log.debug("API: An extra non-free school meal place is now available");
                            }

                            childFound.get().getBookings().remove(booking);
                            booking.getChildrenTakingPart().remove(childFound.get());
                            bookingService.save(booking);
                            childService.save(childFound.get());
                        }

                        Child_summary_DTO child_summary_dto = new Child_summary_DTO();
                        child_summary_dto.setFirstName(childFound.get().getFirstName());
                        child_summary_dto.setLastName(childFound.get().getLastName());
                        child_summary_dto.setIsTakingPart(childDTO.getIsTakingPart());
                        child_summary_dto.setReceivesFreeSchoolMeals(childFound.get().getReceivesFreeSchoolMeals());
                        childSummaryDtoList_return.add(child_summary_dto);
                    } else {
                        log.debug("API: Seems to be problem finding the child");
                        throw new NotFoundException("Could not find child record during booking.");
                    }
                }

                log.debug("API: Looking for parent booking records...");
                for (Parent_summary_username_DTO parentDTO : parentSummaryDTOs) {
                    ParentUser parentUser = userService.findByUsername(parentDTO.getParentUsername()).getParentUser();
                    log.debug("API: Parent, " + parentUser.getFirstName() + " " + parentUser.getLastName() + ", taking part: " + parentDTO.getIsTakingPart());

                    // inform both parents, regardless if they are participating or not, of the booking
                    regParentEmail = parentUser.getEmail();
                    if (parentUser.getPartnerEmail() != null){
                        otherParentEmail = parentUser.getPartnerEmail();
                    }

                    if (parentDTO.getIsTakingPart() && !booking.getParentsTakingPart().contains(parentUser)) {
                        log.debug("API: Adding parent, " + parentDTO.getParentUsername() + ", to booking");
                        someOneTakingPart = true;
                        parentUser.getBookings().add(booking);
                        booking.getParentsTakingPart().add(parentUser);
                        bookingService.save(booking);
                        parentUserService.save(parentUser);
                    } else if (!parentDTO.getIsTakingPart() && booking.getParentsTakingPart().contains(parentUser)){
                        log.debug("API: Removing parent, " + parentDTO.getParentUsername() + ", from booking");
                        parentUser.getBookings().remove(booking);
                        booking.getParentsTakingPart().remove(parentUser);
                        bookingService.save(booking);
                        parentUserService.save(parentUser);
                    }

                    // needed for the return DTO
                    Parent_summary_DTO parent_summary_dto = new Parent_summary_DTO();
                    parent_summary_dto.setParentUsername(parentUser.getUser().getUsername());
                    parent_summary_dto.setFirstName(parentUser.getFirstName());
                    parent_summary_dto.setLastName(parentUser.getLastName());
                    parent_summary_dto.setIsTakingPart(parentDTO.getIsTakingPart());
                    parentSummaryDtoList_return.add(parent_summary_dto);
                }

                if (!someOneTakingPart)
                    log.debug("API: No-one taking part in activity template with uniqueID: " + bookingDTO.getUniqueID());

                log.debug("API: Finalising other booking details...");
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("HH:mm_dd-MM");

                booking.setBookingRef(activityDetail.getActivityTemplate().getUniqueID() + "_"
                        + new SimpleDateFormat("HH:mm_dd-MM-yy").format(activityDetail.getMeetingDate()) + "_"
                        + currentParent.getFirstName().charAt(0) + currentParent.getLastName().charAt(0)
                        + "_" + LocalDateTime.now().format(dateFormatter));

                activityDetail.getBookings().add(booking);
                bookingService.save(booking);

                activityDetailService.findById(bookingDTO.getActivityDetailID()).notifyAll();
                log.debug("API: Activity booking transaction complete. Suspension lifted.");
            }

            activityDetailService.save(activityDetail);
            BookingReplyDTO bookingReplyDTO = new BookingReplyDTO();
            bookingReplyDTO.setBookRef(booking.getBookingRef());
            bookingReplyDTO.setActivityName(activityDetail.getActivityTemplate().getName());

            if (activityDetail.getOrganiser().getStaffUser() != null) {
                StaffUser organiser = activityDetail.getOrganiser().getStaffUser();
                bookingReplyDTO.setOrganiser(organiser.getFirstName() + " " + organiser.getLastName());
            } else {
                AdminUser adminUser = activityDetail.getOrganiser().getAdminUser();
                bookingReplyDTO.setOrganiser(adminUser.getFirstName() + " " + adminUser.getLastName());
            }

            bookingReplyDTO.setDescription(activityDetail.getActivityTemplate().getDescription());
            bookingReplyDTO.setUrl(activityDetail.getActivityTemplate().getUrl());
            bookingReplyDTO.setMeetingDateTime(activityDetail.getMeetingDateTime());
            bookingReplyDTO.setMeetingPlace(AddressMapper.INSTANCE.addressToAddressDTO(activityDetail.getMeetingPlace()));
            bookingReplyDTO.setOtherSupervisors(activityDetail.getOtherSupervisors());

            FamilyDTO familyDTO = new FamilyDTO();
            familyDTO.setParent_summary_dtoList(new Parent_summary_DTOList(parentSummaryDtoList_return));
            familyDTO.setChildSummaryDtoList(new Child_summary_DTOList(childSummaryDtoList_return));

            bookingReplyDTO.setFamilyDTO(familyDTO);
            bookingReplyDTOList.getBookingReplyDTOs().add(bookingReplyDTO);

            log.debug("API: Finished processing booking, related to activity template with uniqueID: " + activityDetail.getActivityTemplate().getUniqueID());
        }

        // send email with booking update
        new Thread(new SendBookingUpdate(regParentEmail, otherParentEmail, bookingReplyDTOList)).start();

        return new ResponseEntity<>(bookingReplyDTOList, HttpStatus.OK);
    }

    /**
     * Takes an activity detail list, sorts it by organiser name and activity template uniqueID and returns a DTO of all bookings for the currentParent and otherParent
     */
    private void printOutBookings(BookingDTOList bookingDTOList, ParentUser currentParent, ParentUser otherParent, Set<Child> currentParentChildren, Set<ActivityDetail> activityDetails) {
        List<ActivityDetail> sortedActivityDetails = new ArrayList<>(activityDetails);
        Collections.sort(sortedActivityDetails);

        for (ActivityDetail activityDetail : sortedActivityDetails) {
            BookingDTO bookingDTO = new BookingDTO();
            bookingDTO.setActivityDetailID(activityDetail.getId());
            bookingDTO.setActivityName(activityDetail.getActivityTemplate().getName());
            bookingDTO.setDescription(activityDetail.getActivityTemplate().getDescription());
            bookingDTO.setUniqueID(activityDetail.getActivityTemplate().getUniqueID());

            User organiser = userService.findByUsername(activityDetail.getOrganiser().getUsername());
            if (organiser.getRole().equals(ADMIN_ROLE)) {
                bookingDTO.setOrganiser(organiser.getAdminUser().getFirstName() + " " + organiser.getAdminUser().getLastName());
            } else
                bookingDTO.setOrganiser(organiser.getStaffUser().getFirstName() + " " + organiser.getStaffUser().getLastName());

            bookingDTO.setUrl(activityDetail.getActivityTemplate().getUrl());
            bookingDTO.setFreeMealPlacesRemaining(activityDetail.getFreeMealPlacesLimit() - activityDetail.getNonFreeMealPlacesTaken());
            bookingDTO.setNonFreeMealPlacesRemaining(activityDetail.getNonFreeMealPlacesLimit() - activityDetail.getNonFreeMealPlacesTaken());

            bookingDTO.setMeetingDateTime(activityDetail.getMeetingDateTime());
            bookingDTO.setMeetingPlace(AddressMapper.INSTANCE.addressToAddressDTO(activityDetail.getMeetingPlace()));
            bookingDTO.setOtherSupervisors(activityDetail.getOtherSupervisors());

            // get all bookings for given activity detail and filter for this family, referencing currentParent
            Set<Booking> bookings = activityDetail.getBookings();
            List<Parent_summary_DTO> parentSummaryDTOs = new ArrayList<>();
            List<Child_summary_DTO> childSummaryDTOs = new ArrayList<>();

            // if bookings is empty then just initialise with blank fields, otherwise process them all
            if (bookings.isEmpty()) {
                bookingDTO.setBookRef(null);

                Parent_summary_DTO currentParentDTO = new Parent_summary_DTO();
                currentParentDTO.setFirstName(currentParent.getFirstName());
                currentParentDTO.setLastName(currentParent.getLastName());
                currentParentDTO.setIsTakingPart(false);
                currentParentDTO.setParentUsername(getUsername());
                parentSummaryDTOs.add(currentParentDTO);

                if (otherParent != null) {
                    Parent_summary_DTO otherParentDTO = new Parent_summary_DTO();
                    otherParentDTO.setFirstName(otherParent.getFirstName());
                    otherParentDTO.setLastName(otherParent.getFirstName());
                    otherParentDTO.setIsTakingPart(false);
                    otherParentDTO.setParentUsername(otherParent.getUser().getUsername());
                    parentSummaryDTOs.add(otherParentDTO);
                }

                for (Child child : currentParentChildren) {
                    Child_summary_DTO child_summary_dto = new Child_summary_DTO();
                    child_summary_dto.setFirstName(child.getFirstName());
                    child_summary_dto.setLastName(child.getLastName());
                    child_summary_dto.setReceivesFreeSchoolMeals(child.getReceivesFreeSchoolMeals());
                    child_summary_dto.setIsTakingPart(false);
                    childSummaryDTOs.add(child_summary_dto);
                }
            } else {
                // there are bookings, go through each as relevant to this parent's family;
                for (Booking booking : bookings) {
                    bookingDTO.setBookRef(booking.getBookingRef());

                    Set<ParentUser> parentsTakingPart = booking.getParentsTakingPart();
                    Parent_summary_DTO currentParentDTO = new Parent_summary_DTO();
                    currentParentDTO.setFirstName(currentParent.getFirstName());
                    currentParentDTO.setLastName(currentParent.getLastName());
                    currentParentDTO.setParentUsername(getUsername());

                    if (parentsTakingPart.contains(currentParent)) {
                        // if at least one is participating, set the booking ref accordingly
                        bookingDTO.setBookRef(booking.getBookingRef());
                        currentParentDTO.setIsTakingPart(true);
                    } else
                        currentParentDTO.setIsTakingPart(false);

                    parentSummaryDTOs.add(currentParentDTO);

                    if (otherParent != null) {
                        Parent_summary_DTO otherParentDTO = new Parent_summary_DTO();
                        otherParentDTO.setFirstName(otherParent.getFirstName());
                        otherParentDTO.setLastName(otherParent.getFirstName());

                        if (parentsTakingPart.contains(otherParent)) {
                            // if at least one is participating, set the booking ref accordingly
                            bookingDTO.setBookRef(booking.getBookingRef());
                            otherParentDTO.setIsTakingPart(true);
                        } else
                            otherParentDTO.setIsTakingPart(false);

                        parentSummaryDTOs.add(otherParentDTO);
                    }

                    Set<Child> childrenTakingPart = booking.getChildrenTakingPart();

                    // now find out which children are taking part
                    for (Child child : currentParentChildren) {
                        Child_summary_DTO child_summary_dto = new Child_summary_DTO();
                        child_summary_dto.setFirstName(child.getFirstName());
                        child_summary_dto.setLastName(child.getLastName());
                        child_summary_dto.setReceivesFreeSchoolMeals(child.getReceivesFreeSchoolMeals());

                        if (childrenTakingPart.contains(child)) {
                            // if at least one is participating, set the booking ref accordingly
                            bookingDTO.setBookRef(booking.getBookingRef());
                            child_summary_dto.setIsTakingPart(true);
                        } else
                            child_summary_dto.setIsTakingPart(false);
                        childSummaryDTOs.add(child_summary_dto);
                    }
                }
            }
            FamilyDTO familyDTO = new FamilyDTO();
            familyDTO.setParent_summary_dtoList(new Parent_summary_DTOList(parentSummaryDTOs));
            familyDTO.setChildSummaryDtoList(new Child_summary_DTOList(childSummaryDTOs));

            bookingDTO.setFamilyDTO(familyDTO);
            bookingDTOList.getBookingDTOList().add(bookingDTO);
        }
    }

    class SendBookingUpdate implements Runnable {
        String regParentEmail;
        String otherParentEmail;
        BookingReplyDTOList bookingReplyDTOList;

        public SendBookingUpdate(String regParentEmail, String otherParentEmail, BookingReplyDTOList bookingReplyDTOList) {
            this.regParentEmail = regParentEmail;
            this.otherParentEmail = otherParentEmail;
            this.bookingReplyDTOList = bookingReplyDTOList;
        }

        @Override
        public void run() {
            try {
                emailService.sendParentsChangesToBookings(regParentEmail, otherParentEmail, bookingReplyDTOList);
            } catch (MessagingException e){
                log.debug("API: Problem sending email: " + e.getMessage());
            }
        }
    }

    /**
     * Used to determine if the current user is a parent and also if they have had their account activated by their BTM rep
     */
    private void verifyParentUserAccess() {
        ParentUser parentUser = userService.findByUsername(getUsername()).getParentUser();

        if (parentUser != null){
            log.debug("Current user with username: " + getUsername() + " is a parent. Checking if they are permitted to access this resource...");

            if (!parentUser.getCanActivateAccount() || !parentUser.getCanUploadChildDataMakeBookings()){
                throw new AccessDeniedException("Current parent is not allowed to access this resource");
            }
        }
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
}

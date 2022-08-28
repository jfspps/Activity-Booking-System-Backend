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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import uk.org.breakthemould.config.ControllerConstants;
import uk.org.breakthemould.domain.DTO.activity.ActivityTemplateDTO;
import uk.org.breakthemould.domain.DTO.activity.ActivityTemplateDTOList;
import uk.org.breakthemould.domain.DTO.activity.ActivityTemplate_new_DTO;
import uk.org.breakthemould.domain.DTO.activity.ActivityTemplate_put_DTO;
import uk.org.breakthemould.domain.activity.ActivityTemplate;
import uk.org.breakthemould.domain.mapper.ActivityTemplateMapper;
import uk.org.breakthemould.domain.security.User;
import uk.org.breakthemould.exception.domain.ActivityTemplateUniqueIDAlreadyExistsException;
import uk.org.breakthemould.exception.domain.BadJSONBodyException;
import uk.org.breakthemould.exception.domain.NotFoundException;
import uk.org.breakthemould.service.ActivityTemplateService;
import uk.org.breakthemould.service.UserService;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@ResponseBody
@Tag(name = "activity-template-controller", description = "Handles all activity template related routes")
@RequestMapping(path = ControllerConstants.ROOT_URL_V1 + "/activityTemplate")
public class ActivityTemplateController {

    private final ActivityTemplateService activityTemplateService;
    private final UserService userService;
    private final ActivityTemplateMapper activityTemplateMapper;

    /**
     * Get a list of all activity templates on file; sorted by uniqueID
     */
    @Operation(summary = "Retrieve a list of all activity templates on file; sorted by uniqueID. STAFF and ADMIN only.")
    @PreAuthorize("hasAnyAuthority('admin.read', 'staff.read')")
    @GetMapping("/")
    public ResponseEntity<ActivityTemplateDTOList> getAllTemplates(){
        log.debug("--------- GET /activityTemplate: list of activity templates requested ---------");

        List<ActivityTemplate> templates = new ArrayList<>(activityTemplateService.findAll());
        Collections.sort(templates);

        ActivityTemplateDTOList activityTemplateDTOList = new ActivityTemplateDTOList();

        templates.forEach(template -> activityTemplateDTOList.getActivityTemplateDTOs().add(ActivityTemplateMapper.INSTANCE.activityTemplateToActivityTemplateDTO(template)));

        return new ResponseEntity<>(activityTemplateDTOList, HttpStatus.OK);
    }

    /**
     * Get a list of all activity templates owned by user with username provided; sorted by uniqueID
     */
    @Operation(summary = "Get a list of all activity templates owned by user with username provided; sorted by uniqueID. STAFF and ADMIN only.")
    @PreAuthorize("hasAnyAuthority('admin.read', 'staff.read')")
    @GetMapping("/{username}/owner")
    public ResponseEntity<ActivityTemplateDTOList> getAllTemplatesByOwner(@PathVariable("username") String username){
        log.debug("--------- GET /activityTemplate/" + username + "/owner: activity template list for user, " + username + ", requested ---------");
        User owner = userService.findByUsername(username);

        List<ActivityTemplate> templates = new ArrayList<>(activityTemplateService.findAllByOwner(owner));
        Collections.sort(templates);

        ActivityTemplateDTOList activityTemplateDTOList = new ActivityTemplateDTOList();

        templates.forEach(template -> activityTemplateDTOList.getActivityTemplateDTOs().add(ActivityTemplateMapper.INSTANCE.activityTemplateToActivityTemplateDTO(template)));

        return new ResponseEntity<>(activityTemplateDTOList, HttpStatus.OK);
    }

    /**
     * Submit a new activity template
     */
    @Operation(summary = "Retrieve a new activity template by database ID. STAFF and ADMIN only.")
    @PreAuthorize("hasAnyAuthority('admin.read', 'staff.read')")
    @GetMapping("/{ID}/database")
    public ResponseEntity<ActivityTemplateDTO> getTemplateByDatabaseID(@PathVariable("ID") String id){
        log.debug("--------- GET /activityTemplate/" + id + "/database: activity template with id " + id + " requested ---------");
        ActivityTemplate found = activityTemplateService.findById(Long.valueOf(id));

        ActivityTemplateDTO activityTemplateDTO = activityTemplateMapper.activityTemplateToActivityTemplateDTO(found);
        return new ResponseEntity<>(activityTemplateDTO, HttpStatus.OK);
    }

    /**
     * Update an existing activity template; note that the uniqueID cannot be edited. New owner field is optional.
     */
    @Operation(summary = "Update a new activity template by database ID. Restricted to template owner. UniqueID cannot be edited. New owner field is optional.")
    @PutMapping("/{ID}/database")
    public ResponseEntity<ActivityTemplateDTO> updateTemplateByDatabaseID(@PathVariable("ID") String id,
                                                                          @RequestBody ActivityTemplate_put_DTO activityTemplate_put_dto) throws NotFoundException {
        log.debug("--------- PUT /activityTemplate/" + id + "/database: update to activity template with id " + id + " requested ---------");

        // check for activity template name (mandatory property)
        String name = activityTemplate_put_dto.getName();
        if (name == null || name.isBlank()){
            throw new BadJSONBodyException("Missing activity template name");
        } else
            log.debug("API: Activity template name received");

        String uniqueID = activityTemplate_put_dto.getUniqueID();
        if (!activityTemplateService.activityTemplateWithUniqueIDExists(uniqueID)){
            throw new NotFoundException("Activity template with uniqueID: " + uniqueID + " not on file");
        } else
            log.debug("API: Activity template record with given uniqueID found");

        ActivityTemplate found = activityTemplateService.findById(Long.valueOf(id));

        if (!found.getOwner().getUsername().equals(getUsername())){
            throw new AccessDeniedException("Only activity template owner may edit this template");
        } else
            log.debug("API: Current user is authorised to edit this template");

        if (!found.getUniqueID().equals(activityTemplate_put_dto.getUniqueID())){
            throw new BadJSONBodyException("Activity template uniqueID and database ID do not refer to the same record");
        } else
            log.debug("API: Database ID and uniqueID point to the same record");

        log.debug("API: Checks done, processing updates...");
        found.setName(activityTemplate_put_dto.getName());
        found.setDescription(activityTemplate_put_dto.getDescription());
        found.setUrl(activityTemplate_put_dto.getUrl());

        String newOwnerUsername = activityTemplate_put_dto.getNewOwner().getUsername();

        if (newOwnerUsername != null && !newOwnerUsername.isBlank()){
            try {
                User newOwner = userService.findByUsername(newOwnerUsername);
                found.setOwner(newOwner);
                log.debug("API: New activity template owner changed");
                // todo: send email to new owner
            } catch (NoResultException exception){
                log.debug("API: New owner username invalid. Could not update activity template's new owner property. Current owner is: " + getUsername());
            }
        } else
            log.debug("API: New owner field was blank; owner of template not changed");

        ActivityTemplate saved = activityTemplateService.save(found);

        ActivityTemplateDTO activityTemplateDTO = activityTemplateMapper.activityTemplateToActivityTemplateDTO(saved);
        return new ResponseEntity<>(activityTemplateDTO, HttpStatus.OK);
    }

    /**
     * Retrieve an activity template by uniqueID
     */
    @Operation(summary = "Retrieve a new activity template by uniqueID. STAFF and ADMIN only.")
    @PreAuthorize("hasAnyAuthority('admin.read', 'staff.read')")
    @GetMapping("/byUniqueID")
    public ResponseEntity<ActivityTemplateDTO> getTemplateByUniqueID(@RequestParam("uniqueId") String uniqueID){
        log.debug("--------- GET /activityTemplate/byUniqueID: activity template with uniqueID, " + uniqueID + ", requested ---------");
        ActivityTemplate found = activityTemplateService.findByUniqueID(uniqueID);

        ActivityTemplateDTO activityTemplateDTO = activityTemplateMapper.activityTemplateToActivityTemplateDTO(found);
        return new ResponseEntity<>(activityTemplateDTO, HttpStatus.OK);
    }

    /**
     * Submit a new activity template (name and unqiue ID are mandatory)
     */
    @Operation(summary = "Submit a new activity template (activity template name and uniqueID are mandatory). STAFF and ADMIN only.")
    @PreAuthorize("hasAnyAuthority('admin.create', 'staff.create')")
    @PostMapping("/")
    public ResponseEntity<ActivityTemplateDTO> newTemplate(@RequestBody ActivityTemplate_new_DTO activityTemplate_new_dto){
        log.debug("--------- POST /activityTemplate: new activity template submitted ---------");

        String name = activityTemplate_new_dto.getName();
        String uniqueID = activityTemplate_new_dto.getUniqueID();

        if (name == null || name.isBlank() || uniqueID == null || uniqueID.isBlank()){
            throw new BadJSONBodyException("Missing activity template name and/or uniqueID");
        }

        if (activityTemplateService.activityTemplateWithUniqueIDExists(uniqueID)){
            throw new ActivityTemplateUniqueIDAlreadyExistsException("UniqueID: " + uniqueID + " already in use");
        }

        log.debug("API: Checks done, processing details...");
        ActivityTemplate template = ActivityTemplate.builder()
                .description(activityTemplate_new_dto.getDescription())
                .name(activityTemplate_new_dto.getName())
                .uniqueID(activityTemplate_new_dto.getUniqueID())
                .url(activityTemplate_new_dto.getUrl())
                .owner(userService.findByUsername(getUsername()))
                .build();

        ActivityTemplate saved = activityTemplateService.save(template);
        ActivityTemplateDTO activityTemplateDTO = activityTemplateMapper.activityTemplateToActivityTemplateDTO(saved);
        return new ResponseEntity<>(activityTemplateDTO, HttpStatus.OK);
    }

    /**
     * Retrieves the username of the authenticated user
     */
    private String getUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails){
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }
}

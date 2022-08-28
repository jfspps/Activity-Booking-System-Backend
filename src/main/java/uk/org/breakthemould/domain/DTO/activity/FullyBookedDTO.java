package uk.org.breakthemould.domain.DTO.activity;

import lombok.Data;

@Data
public class FullyBookedDTO {
    private String message = "We regret to inform you that due to limited places, the activities you are interested in are already full booked. The last few places may have just been taken.\n" +
            "We will contact you if a place becomes available.\n" +
            "If you have any questions or need any further information, contact kris@breakthemould.org";
}

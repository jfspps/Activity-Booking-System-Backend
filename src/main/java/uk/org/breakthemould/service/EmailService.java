package uk.org.breakthemould.service;

import com.sun.mail.smtp.SMTPTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.org.breakthemould.config.EmailSettings;
import uk.org.breakthemould.domain.DTO.activity.BookingReplyDTO;
import uk.org.breakthemould.domain.DTO.activity.BookingReplyDTOList;
import uk.org.breakthemould.domain.DTO.activity.Child_summary_DTO;
import uk.org.breakthemould.domain.DTO.details.AddressDTO;
import uk.org.breakthemould.domain.DTO.personnel.FamilyDTO;
import uk.org.breakthemould.domain.DTO.personnel.Parent_summary_DTO;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static uk.org.breakthemould.config.EmailSettings.*;

@Service
@Slf4j
public class EmailService {

    private final EmailSettings emailSettings;

    public EmailService(EmailSettings emailSettings) {
        this.emailSettings = emailSettings;
    }

    public void sendParentsChangesToBookings(String regParentEmail, String otherParentEmail, BookingReplyDTOList bookingReplyDTOList) throws MessagingException {
        // build the booking string
        StringBuilder sb = new StringBuilder();
        List<BookingReplyDTO> bookings = bookingReplyDTOList.getBookingReplyDTOs();
        bookings.forEach(bookingReplyDTO -> {
            sb.append("BOOKING REFERENCE: ").append(bookingReplyDTO.getBookRef()).append(" =====================\n");
            sb.append("Activity: ").append(bookingReplyDTO.getActivityName().toUpperCase()).append(" organised by: ").append(bookingReplyDTO.getOrganiser()).append("\n");
            if (!bookingReplyDTO.getDescription().isBlank()){
                sb.append("Activity description: ").append(bookingReplyDTO.getDescription()).append("\n");
            }
            if (!bookingReplyDTO.getOtherSupervisors().isBlank()){
                sb.append("Other supervisors taking part: ").append(bookingReplyDTO.getOtherSupervisors()).append("\n");
            }
            if (!bookingReplyDTO.getUrl().isBlank()){
                sb.append("For more info, please go to: ").append(bookingReplyDTO.getUrl()).append("\n\n");
            }

            sb.append("Activity TIME and DATE: ").append(new SimpleDateFormat("HH:mm, dd MMMM yyyy").format(bookingReplyDTO.getMeetingDateTime()).toUpperCase()).append("\n\n");
            AddressDTO meetingPlace = bookingReplyDTO.getMeetingPlace();
            sb.append("Activity meeting PLACE: ")
                    .append(meetingPlace.getFirstLine())
                    .append(", ")
                    .append(meetingPlace.getSecondLine())
                    .append(", ")
                    .append(meetingPlace.getTownCity())
                    .append(", ")
                    .append(meetingPlace.getPostCode()).append("\n\n");

            FamilyDTO familyDTO = bookingReplyDTO.getFamilyDTO();
            List<Parent_summary_DTO> parents = familyDTO.getParent_summary_dtoList().getParentSummaryDTOs();
            List<Child_summary_DTO> children = familyDTO.getChildSummaryDtoList().getChildSummaryDTOs();
            sb.append("Confirmed places reserved for:\n");

            List<Parent_summary_DTO> parentsIn = parents.stream()
                    .filter(Parent_summary_DTO::getIsTakingPart)
                    .collect(Collectors.toList());

            List<Child_summary_DTO> childrenIn = children.stream()
                    .filter(Child_summary_DTO::getIsTakingPart)
                    .collect(Collectors.toList());

            if (parentsIn.isEmpty() && childrenIn.isEmpty()){
                sb.append("(No confirmed reservations)").append("\n");
            } else {
                parentsIn.forEach(parent_summary_dto -> {
                    sb.append(parent_summary_dto.getFirstName()).append(" ").append(parent_summary_dto.getLastName()).append("\n");
                });
                childrenIn.forEach(child_summary_dto -> {
                    sb.append(child_summary_dto.getFirstName()).append(" ").append(child_summary_dto.getLastName()).append("\n");
                });
            }
            sb.append("----------------------------------------------------------------------------------------------------------------------------\n");
        });

        String booking = sb.toString();

        // todo: remove at release
        log.debug("EmailService.sendParentsChangesToBookings ============================================================================");
        log.debug(booking);

        log.debug("Preparing to email message to " + regParentEmail + " ============================================================================");

        Message message = informParentBookingsUpdate(regParentEmail, booking);
        smtpTransport(message);
        log.debug("Reg parent informed of changes to bookings. Email sent to: " + regParentEmail);

        if (otherParentEmail != null && !otherParentEmail.isBlank()){
            log.debug("Preparing to send email to " + otherParentEmail + " ============================================================================");

            message = informParentBookingsUpdate(otherParentEmail, booking);
            smtpTransport(message);
            log.debug("Other parent informed of changes to bookings. Email sent to: " + otherParentEmail);
        }
    }

    public void sendBTMrepAndParentsChangesToChildData(String btmRepEmail, String regParentUsername, String regParentEmail, String otherParentUsername, String otherParentEmail) throws MessagingException {
        Message message = informBTMrepChildDataChanged(btmRepEmail, regParentUsername);
        smtpTransport(message);
        log.debug("BTM rep informed of changes to child data. Email sent to: " + btmRepEmail);

        message = informParentChildDataChanged(regParentEmail);
        smtpTransport(message);
        log.debug("Registering parent informed of changes to child data. Email sent to: " + regParentEmail);

        if (otherParentUsername != null && !otherParentUsername.isBlank() && otherParentEmail != null && !otherParentEmail.isBlank()){
            message = informParentChildDataChanged(otherParentEmail);
            smtpTransport(message);
            log.debug("Other parent informed of changes to child data. Email sent to: " + otherParentEmail);
        }
    }

    public void sendBTMrepParentAccountActivated(String btmRepEmail, String regParentUsername, String regParentEmail,
                                                 String otherParentUsername, String otherParentEmail) throws MessagingException {
        Message message = informBTMRepAccountsActive(btmRepEmail, regParentUsername, regParentEmail, otherParentUsername, otherParentEmail);
        smtpTransport(message);
        log.debug("BTM rep notification of activated accounts sent. Email sent to: " + btmRepEmail);
    }

    public void sendAccountActivatedEmail(String email) throws MessagingException {
        Message message = accountActivatedEmail(email);
        smtpTransport(message);
        log.debug("Update personal details email sent. Email sent to: " + email);
    }

    public void sendUpdatePersonalDetailsEmail(String email) throws MessagingException {
        Message message = updatePersonalDetailsEmail(email);
        smtpTransport(message);
        log.debug("Update personal details email sent. Email sent to: " + email);
    }

    public void sendRegParentActivationEmail(String email) throws MessagingException {
        Message message = regParentPreActivateEmail(email);
        smtpTransport(message);
        log.debug("Registering parent pre-activate email sent. Email sent to: " + email);
    }

    public void sendRegParentOtherParentAccountOngoing(String email) throws MessagingException {
        Message message = regParentAwaitingOtherParentDetailEmail(email);
        smtpTransport(message);
        log.debug("Registering parent informed that other parent will be contacted. Email sent to: " + email);
    }

    public void informRegParentOtherParentSentDetailsEmail(String email) throws MessagingException {
        Message message = regParentInformedOtherParentSentDetails(email);
        smtpTransport(message);
        log.debug("Registering parent informed that other parent was emailed. Email sent to: " + email);
    }

    public void informBTMRepNewAccountEmail(String btmRepEmail, String parentUsername) throws MessagingException {
        Message message = btmRepInformNewParentEmail(btmRepEmail, parentUsername);
        smtpTransport(message);
        log.debug("BTM rep informed of new parent details. Email sent to: " + btmRepEmail);
    }

    public void sendNewPasswordEmail(String password, String email) throws MessagingException {
        Message message = createNewPasswordEmail(password, email);
        smtpTransport(message);
        log.debug("New password email sent. Email sent to: " + email);
    }

    public void sendResetPasswordEmail(String password, String email) throws MessagingException {
        Message message = resetPasswordEmail(password, email);
        smtpTransport(message);
        log.debug("Reset password email sent. Email sent to: " + email);
    }

    public void sendNewUsernameEmail(String username, String email) throws MessagingException {
        Message message = createNewUsernameEmail(username, email);
        smtpTransport(message);
        log.debug("New username email sent. Email sent to: " + email);
    }

    private Message informBTMrepChildDataChanged(String btmRepEmail, String regParentUsername) throws MessagingException {
        Message message = new MimeMessage(getEmailSession());

        message.setFrom(new InternetAddress(emailSettings.getFrom_email()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(btmRepEmail, false));
        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC_EMAIL, false));
        message.setSubject(EMAIL_SUBJECT);

        message.setText(
                "Hello, " +
                        "\n\nWe are informing you that the child/children records of a parent, with an account username:\"" +
                        regParentUsername + "\", have been edited." +
                        "\n\nThe BTM support team");
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    private Message informParentBookingsUpdate(String parentEmail, String booking) throws MessagingException {
        Message message = new MimeMessage(getEmailSession());

        message.setFrom(new InternetAddress(emailSettings.getFrom_email()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(parentEmail, false));
        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC_EMAIL, false));
        message.setSubject(EMAIL_SUBJECT);

        message.setText(
                "Hello, " +
                        "\n\nThank you for your booking update onto Summer Fun 2021. We can’t wait for the activities to begin.\n\n" + booking + "\n\n"
                        + "We look forward to seeing you there \uD83D\uDE0A. Please check the list of names above for confirmed places." +
                        " If you submitted a reservation and a name is not listed above then unfortunately the activities " +
                        "you are interested in are already fully booked. The last few places may have just been taken. " +
                        "We will contact you if a place becomes available.\n\n" +
                        "If you have any questions or need any further information, please contact kris@breakthemould.org.\n\n" +
                        "\n\nThe BTM support team");
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    private Message informParentChildDataChanged(String parentEmail) throws MessagingException {
        Message message = new MimeMessage(getEmailSession());

        message.setFrom(new InternetAddress(emailSettings.getFrom_email()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(parentEmail, false));
        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC_EMAIL, false));
        message.setSubject(EMAIL_SUBJECT);

        message.setText(
                "Hello, " +
                        "\n\nWe are informing you that your child/children records have been edited. If you believe " +
                        "this to be in error or the details are incorrect, please do not hesitate to contact your BTM representative." +
                        "\n\nThe BTM support team");
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    private Message informBTMRepAccountsActive(String btmRepEmail, String regParentUsername, String regParentEmail,
                                               String otherParentUsername, String otherParentEmail) throws MessagingException {
        Message message = new MimeMessage(getEmailSession());

        message.setFrom(new InternetAddress(emailSettings.getFrom_email()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(btmRepEmail, false));
        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC_EMAIL, false));
        message.setSubject(EMAIL_SUBJECT);

        String reply;
        if (otherParentUsername != null || otherParentEmail != null){
            reply = "Registering parent username: \"" + regParentUsername + "\" with email: " + regParentEmail + "\n"
                    + "Other parent username: \"" + otherParentUsername + "\" with email: " + otherParentEmail + "\n";
        } else {
            reply = "Parent username: \"" + regParentUsername + "\" with email: " + regParentEmail + "\n";
        }

        message.setText(
                "Hello, " +
                        "\n\nWe are informing you that the following parent accounts are now active:" +
                        "\n\n" + reply +
                        "\nThe BTM support team");
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    private Message accountActivatedEmail(String email) throws MessagingException {
        Message message = new MimeMessage(getEmailSession());

        message.setFrom(new InternetAddress(emailSettings.getFrom_email()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC_EMAIL, false));
        message.setSubject(EMAIL_SUBJECT);

        message.setText(
                "Hello, " +
                        "\n\nWe are pleased to inform you that your booking system account has been ACTIVATED." +
                        "\n\nYou may now begin uploading child details and make BOOKINGS." +
                        "\n\nThe BTM support team");
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    private Message resetPasswordEmail(String password, String email) throws MessagingException {
        Message message = new MimeMessage(getEmailSession());

        message.setFrom(new InternetAddress(emailSettings.getFrom_email()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC_EMAIL, false));
        message.setSubject(EMAIL_SUBJECT);

        message.setText(
                "Hello, " +
                        "\n\nWe have received your instruction to reset your password. " +
                        "\n\nYour new BTM activity booking account password is: " + password +
                        "\n\nThe BTM support team");
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    private Message createNewPasswordEmail(String password, String email) throws MessagingException {
        Message message = new MimeMessage(getEmailSession());

        message.setFrom(new InternetAddress(emailSettings.getFrom_email()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC_EMAIL, false));
        message.setSubject(EMAIL_SUBJECT);

        // todo: set the booking system domain
        message.setText(
                "Hello, " +
                        "\n\nA new account has been prepared for you. For security reasons, your username and password " +
                        "are being sent in separate emails. Please go to BlaBlaBla to enter your details. " +
                        "\n\nYour new BTM activity booking account password is: " + password +
                        "\n\nThe BTM support team");
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    private Message createNewUsernameEmail(String username, String email) throws MessagingException {
        Message message = new MimeMessage(getEmailSession());

        message.setFrom(new InternetAddress(emailSettings.getFrom_email()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC_EMAIL, false));
        message.setSubject(EMAIL_SUBJECT);

        // todo: set the booking system domain
        message.setText(
                "Hello, " +
                        "\n\nA new account has been prepared for you. For security reasons, your username and password " +
                        "are being sent in separate emails. Please go to BlaBlaBla to enter your details. " +
                        "\n\nYour new BTM activity booking account username is: " + username +
                        "\n\nThe BTM support team");
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    private Message updatePersonalDetailsEmail(String email) throws MessagingException {
        Message message = new MimeMessage(getEmailSession());

        message.setFrom(new InternetAddress(emailSettings.getFrom_email()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC_EMAIL, false));
        message.setSubject(EMAIL_SUBJECT);

        message.setText(
                "Hello, " +
                        "\n\nWe are notifying you that the booking system has received instruction to update your personal details. " +
                        "\n\nIf you believe this to be in error, please do not hesitate to contact BTM." +
                        "\n\nThe BTM support team");
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    private Message regParentPreActivateEmail(String email) throws MessagingException {
        Message message = new MimeMessage(getEmailSession());

        message.setFrom(new InternetAddress(emailSettings.getFrom_email()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC_EMAIL, false));
        message.setSubject(EMAIL_SUBJECT);

        message.setText(
                "Hello, " +
                        "\n\nWe have received your personal details and have informed your BTM representative to verify  " +
                        "\nyour information. When your BTM representative has activated your account you will then " +
                        "\nbe able to upload other personal details for your family and make BOOKINGS." +
                        "\n\nThe BTM support team");
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    private Message regParentAwaitingOtherParentDetailEmail(String email) throws MessagingException {
        Message message = new MimeMessage(getEmailSession());

        message.setFrom(new InternetAddress(emailSettings.getFrom_email()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC_EMAIL, false));
        message.setSubject(EMAIL_SUBJECT);

        message.setText(
                "Hello, " +
                        "\n\nWe have received your personal details and have informed your BTM representative to verify " +
                        "\nyour information. Your BTM representative will set up an account for your partner/spouse using " +
                        "\nthe email address you have provided and ask them to submit their details for registration." +
                        "\n\nThe BTM support team");
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    private Message regParentInformedOtherParentSentDetails(String email) throws MessagingException {
        Message message = new MimeMessage(getEmailSession());

        message.setFrom(new InternetAddress(emailSettings.getFrom_email()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC_EMAIL, false));
        message.setSubject(EMAIL_SUBJECT);

        message.setText(
                "Hello, " +
                        "\n\nWe have received the personal details of your partner/spouse and have informed your BTM representative to verify " +
                        "\ntheir information. When your BTM representative has activated your accounts you will both " +
                        "\nbe able to upload other personal details for your family and make BOOKINGS." +
                        "\n\nThe BTM support team");
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    private Message btmRepInformNewParentEmail(String btmRepEmail, String parentUsername) throws MessagingException {
        Message message = new MimeMessage(getEmailSession());

        message.setFrom(new InternetAddress(emailSettings.getFrom_email()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(btmRepEmail, false));
        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC_EMAIL, false));
        message.setSubject(EMAIL_SUBJECT);

        message.setText(
                "Hello, " +
                        "\n\nA parent with username: \"" + parentUsername + "\" has submitted their details to the Booking " +
                        "\nsystem database." +
                        "\n\nThe BTM support team");
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    private void smtpTransport(Message message) throws MessagingException {
        SMTPTransport smtpTransport = (SMTPTransport) getEmailSession().getTransport(SIMPLE_MAIL_TRANSFER_PROTOCOL);
        smtpTransport.connect(
                emailSettings.getEmail_smtp_server(),
                emailSettings.getUsername(),
                emailSettings.getPassword());
        smtpTransport.sendMessage(message, message.getAllRecipients());
        smtpTransport.close();
        log.debug("Email sent from: " + emailSettings.getUsername());
    }

    private Session getEmailSession(){
        Properties properties = System.getProperties();

        properties.put(SMTP_HOST, emailSettings.getEmail_smtp_server());
        properties.put(SMTP_AUTH, true);
        properties.put(SMTP_PORT, emailSettings.getEmail_smtp_port());
        properties.put(emailSettings.getEmail_smtp_tls_enable(), true);
        properties.put(emailSettings.getEmail_smtp_tls_required(), true);

        return Session.getInstance(properties, null);
    }
}

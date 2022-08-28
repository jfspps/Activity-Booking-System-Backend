# Intercepting email messages

At certain stages of the user interaction, the back-end generates and sends "no-reply" style emails to users. This occurs
whenever personal or activity related details are initialised or updated.

Given the restrictions email service providers place on the use of third-party email clients (e.g. email applications installed 
on a mobile phone, tablet or computer; the back-end would be classed as a third-party email client) it is necessary for 
development purposes to know what the email message content was in order to access the functions provided by the back-end needed
to continue with front-end development and testing.

For example, when new users are registered to the system, the back-end sends emails to the new user with their username and 
password. Front-end developers and software testers need to know what the password was in order to assume the role of the 
new user and continue with the development of the front-end. Passwords are always encrypted before they are saved to the 
database and the only pragmatic way of knowing what the password was is by intercepting the email message before it gets 
sent and printing it to the console.

## End-points where emails are sent

The following requests, at some point, instruct the back-end to send an email. Note that the initial part of the URL is 
a dynamic environment variable and hence omitted; look to the end of the URL.

### Password reset

+ POST .../users/resetPassword

The email sent following the above request is printed to the console as a debug level message BEFORE the back-end sends 
the email (so should be printed regardless if an email related run-time error occurs):

```
---Please remove this from the release version-----
API: Current username: %someUserName% with new reset password: %someNewPassword%
```

### For parent and child registration requests

+ PUT .../users/registerParent
+ PUT .../users/otherParent
+ POST .../users/activateParentsAccount
+ POST .../users/children

The email message content for the above requests are quite generic and do contain anything (no passwords etc.) which 
would hinder front-end development.

### For system account admin requests

+ POST .../users/admin
+ POST .../users/staff
+ POST .../users/parent

The email message for the above requests contain username and/or password details and have been printed to the console 
to assist front-end development.

```
---Please remove this from the release version-----
API: New admin username: %someAdminUsername% with new password: %someNewPassword%
```

The messages for the registration of staff and parent users follows similarly (substitute "admin" for "staff" or 
"parent" in the console message, as appropriate).

+ PUT .../users/{username}/username

The email message for the above request is a generic notification and does not contain anything that would hinder 
front-end development.

### For activity detail requests

+ PUT .../activityDetail/withBookings

The email sent following the above request can get quite long and is dependent on the number of activity details recorded 
in the message.

First, the console message is printed as:

```
EmailService.sendParentsChangesToBookings ============================================================================
(Email message goes here)
```

Secondly, the registering parent's email address is printed

```
Preparing to email message to %regParentEmail% ============================================================================
```

If the email cannot be sent (quite likely without hosting the back-end on a web host) then error messages will be printed, 
and the following log messages would not be printed.

On the other hand, if an error was not produced on sending the first email then the other parent on file (if present) is 
also emailed, with the same sort of console message:

```
Preparing to email message to %otherParentEmail% ============================================================================
```

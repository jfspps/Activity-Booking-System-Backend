# Booking System

A JWT backed Spring Security booking system for BTM activities.

Current localhost port: 5000 (see [application.yml](./src/main/resources/application.yml))

## Getting started documentation

The basic concepts and workflow involved is outlined in [here](/docs/Getting%20Started.pdf). There are other useful
documents in the same directory that outline the workflow involved.

## Compiled JAR

The precompiled JAR is [here](./JAR). At the console enter

```bash
java -jar breakthemould-x.y.z.jar
```

with the matching versions of x, y and z.

## SMTP emails

The above command will run the backend with the email defaults applied. Optionally, the email accounts/usernames can be 
overridden (currently tested via Gmail) by passing parameters at the command prompt when running the JAR file. For gmail users,
the following will allow one to send emails from their own account:

```bash
java -jar breakthemould-x.y.z.jar --email.username="yourUserName@gmail.com" --email.password="yourPassword"
```

Please see [application.yml](/src/main/resources/application.yml) for the current email defaults and [EmailSettings](/src/main/java/uk/org/breakthemould/config/EmailSettings.java) 
for more details on what else can be overridden.

For details regarding email message content (sent from the back-end), please refer to [this document](Email.md).

## Welcome page

(Build and) run the project, then access the welcome page at [http://localhost:5000/](http://localhost:5000/). The  
landing page lists details about how to access the in-memory H2
 database console as well as provide preloaded usernames and passwords.

## API login and JSON web token (JWT) authorisation

To log in, POST the following JSON (see welcome page for other accounts) as the request body to 
_http://localhost:5000/api/v1/users/login_ (run the app and consult the [openapi docs](http://localhost:5000/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config#/)
 for other JSON request body examples).

```json
{
    "username": "admin",
    "password": "admin123"
}
```

Following authentication, a response header "JWT" is provided with the JSON web token string. The JWT is valid 
for [1 day](./src/main/java/uk/org/breakthemould/jwt/JWTConstants.java) and must be sent as part of subsequent requests
in the request header "Bearer " _i.e._ key = "Bearer " and value = JWT.

The backend will use the JWT to identify the user's credentials. In some cases, however, the user will be expected to supply
their username for the purposes of verification.

## First login attempt

All users who log in to the system for the first time are required to change their (randomly generated) password before 
proceeding. The following JSON will be returned as the body of the response:

```json
{
    "username": "someone",
    "mustChangePassword": true
}
```

On successfully changing their password, their [hasChangedFirstPassword](./src/main/java/uk/org/breakthemould/domain/security/User.java) 
boolean will be set to true and the above JSON response will no longer apply.

## Parent registration

The first parent who submits their personal details is known as the _registering parent_. When an admin or staff user
initially creates an account for a parent, they must also mark the parent as registering or not. This will be needed to 
route registering and non-registering parents at the latter stages, where both will be required to upload key personal 
data.

An ADMIN or STAFF member must enter:

+ Registering parent email address
+ Registering parent username
  
The endpoint of interest is (POST) /users/parent. The backend will then send the username and generated password to the 
registering parent. They are then required to log in, change their default password and then enter their personal details,
which on submission, the frontend can use the request (PUT) /users/registerParent.

Following registration of the registering parent, the JSON response will be based on whether they submitted their
partner's/spouse's email, [partnerEmail](./src/main/java/uk/org/breakthemould/domain/security/ParentUser.java).

```json
{
    "username": "randomChap5",
    "canActivateAccount": true,
    "partnerEmail": null,
    "isRegisteringParent": true,
    "isPartnered": false
}
```

The field `canActivateAccount` indicates if all parents required in a family (could be one sole parent or both parents)
have submitted their personal details. If the registering parent did not submit the email address of their partner then 
this would automatically be set to `true` and the BTM rep is then free to activate their account.

Note that the back-end does not check or validate the information uploaded by the parent. This is for the BTM rep to decide.
If the parent did not upload sufficient details then they should ask the parent to log in again using the same link and
re-upload the form with the information required.

If the family wish to register another parent then the registering parent must submit the email address of the other 
parent (this would also be known to BTM prior to registration). At the same time, the BTM rep will need to set up an account 
for the other parent with the email provided and provide a username. The other parent will also receive an email, and then
log in to the system, change their default password and then enter their own personal details. 

When both parents have submitted their personal details, then both parents' `canActivateAccount` field will be set 
to `true` to indicate to the BTM rep that both parents have uploaded information. The BTM rep would only activate 
the account(s) if they are satisfied that the information is sufficient.

## Parent account activation

Once all personal details for one or both parents have been satisfactorily uploaded, the BTM representative will then 
be required to select "activate account". Following email address and username checks, as well as the registering parent's 
"isPartnered=true" field included, this then sets [canUploadChildDataMakeBookings](./src/main/java/uk/org/breakthemould/domain/security/ParentUser.java) 
to true and allows the parent(s) to begin uploading child data and make bookings. To activate accounts, use the (POST)
/users/activateParentsAccount endpoint.

For families with two registered parents, this step also effectively links ("marries") the two ParentUser entities,
according to their [partner](./src/main/java/uk/org/breakthemould/domain/security/ParentUser.java) property. An example of 
the JSON response is given below:

```json
{
    "canUploadChildDataMakeBookings": true,
    "registeringParentUsername": "parent",
    "getRegisteringParentEmail": "registeringParents@someEmail.com",
    "hasPartner": true,
    "otherParentUsername": "parent2",
    "otherParentEmail": "otherParentsEmail@somewhereElse.com"
}
```

Such a "marriage" basically means that either parent can manage their family's child record(s) and/or make bookings. The 
changes brought about by one partner would be made accessible to the other partner.

## Date and Time format JSON requests

All JSON requests for date and time parameters, according to OpenAPI include date, time down to the millisecond.
```json
{
 "startDate": "2021-08-01T12:39:41.715Z",
 "endDate": "2021-08-06T12:43:41.432Z"
}
```

When sending queries, it is not a requirement to send time data down to the millisecond. The following start and end date-times, down to the minute, will suffice:
```json
{
 "startDate": "2021-08-01T12:39",
 "endDate": "2021-08-06T12:43"
}
```


## Booking activities: general comments

Admin, staff and parents can get all activity details between July 2021 and October 2024, as shown below, with:

```bash
/activityDetail/between?startDateTime=2021-07-10T14:45&endDateTime=2024-10-02T14:45
```

The above command does not list anything about bookings and can be used to display multiple activity details on a calendar.

Parents (only) can get all activity details between dates and times, along with their own bookings (not others), using:

```bash
/activityDetail/withBookings?startDateTime=2021-07-10T14:45&endDateTime=2024-10-02T14:45
```

While not expected, if there are no children registered for a given family then submitting requests for activity details bookings are ignored by 
the back-end, raising a NotFoundException.

### A note on back-end startup/restart and activity detail date-times

Each time the backend is loaded, an example activity detail is saved to the in-memory database and date-stamped at the same time the backend is loaded.
This means that if you want to retrieve a list of activity details on file, you must set the endDate to a date/time after the backend was loaded. If the current date
and time is after 2nd October 2024 at 14:45 and you send the above JSON request, then you will receive a blank list (or empty array).

If the backend was restarted on the 3rd November 2021 (and the example activity detail was initialised on the same day) then the above "/activityDetail/between" endpoint would give a non-empty list of activity details, shown below.

```json
{
 "activityDetails": [
  {
   "id": 1,
   "activityTemplate": {
     "uniqueID": "tennis101",
     "name": "Tennis",
     "description": "A game of tennis",
     "url": "somewhere.com",
     "new_owner": null
   },
   "freeMealPlacesLimit": 25,
   "freeMealPlacesTaken": 0,
   "nonFreeMealPlacesLimit": 7,
   "nonFreeMealPlacesTaken": 0,
   "meetingDateTime": "2021-11-03T17:35:41.331+00:00",
   "organiser": {
    "username": "staff"
   },
   "meetingPlace": {
     "firstLine": "All sports Centre",
     "secondLine": "Queen's Road",
     "townCity": "Loughborough",
     "postCode": "LOUGHBOROUGH"
   },
   "otherSupervisors": "Other people involved"
  }
 ]
}
```

#### Activity detail bookings and null booking references

Parents can view multiple bookings over a given time period, using (GET) /activityDetail/withBookings endpoint explained above.

When the backend is started up, one can assume that no parent has made any bookings. Assuming a parent chose the appropriate time period 
when an activity is scheduled (and the frontend sent GET /activityDetail/withBookings...), they would receive something like this:

```json
{
    "bookings": [
        {
            "activityDetailID": 1,
            "bookRef": null,
            "uniqueID": "tennis101",
            "activityName": "Tennis",
            "organiser": "The Staff",
            "description": "A game of tennis",
            "url": "somewhere.com",
            "freeMealPlacesRemaining": 25,
            "nonFreeMealPlacesRemaining": 7,
            "meetingDateTime": "2021-07-22T19:37:18.417+00:00",
            "meetingPlace": {
                "firstLine": "All sports Centre",
                "secondLine": "Queen's Road",
                "townCity": "Loughborough",
                "postCode": "LOUGHBOROUGH"
            },
            "otherSupervisors": "Other people involved",
            "family": {
                "parentList": {
                    "parents": [
                        {
                            "firstName": "The",
                            "lastName": "Parent",
                            "isTakingPart": false,
                            "parentUsername": "parent"
                        }
                    ]
                },
                "childList": {
                    "children": [
                        {
                            "firstName": "Bob",
                            "lastName": "Smith",
                            "receivesFreeSchoolMeals": false,
                            "isTakingPart": false
                        }
                    ]
                }
            }
        }
    ]
}
```

Note how the bookingRef is __null__ in the above example. This means that the backend has never received a booking request (whatever the parameters) for this activity before.

After a booking has been submitted, then at all other times thereafter, a non-null bookRef is assigned. This includes _subsequent_ requests where the entire 
family withdraws from the activity.

The booking reference itself is updated to reflect the date and time of the last booking, regardless of what request was made. It is therefore very important to ensure that the __incoming 
activity booking requests apply with the latest booking reference__. Each booking reference relates to a single activity detail for the whole family.

#### Example of booking an activity detail for the first time

At this point the parent would make changes to their "null" booking through the frontend with the PUT request. The frontend should send a JSON request, for example

```json
{
  "bookings": [
    {
      "bookingRef": null,
      "activityDetailID": 1,
      "uniqueID": "tennis101",
      "family": {
        "parentList": {
          "parents": [
            {
              "parentUsername": "parent",
              "isTakingPart": false
            }
          ]
        },
        "childList": {
          "children": [
            {
              "firstName": "Bob",
              "lastName": "Smith",
              "isTakingPart": true
            }
          ]
        }
      }
    }
  ]
}
```

Note, again, that the bookingRef is still __null__ because this will be the first booking reference. In this example, the child has requested a place while the parent chose not to 
take part. The backend will, if there are sufficient places at the time, respond with something like:

```json
{
    "bookingSummaries": [
        {
            "bookRef": "tennis101_00:00_22-07-21_TP_20:42_22-07",
            "activityName": "Tennis",
            "organiser": "The Staff",
            "description": "A game of tennis",
            "url": "somewhere.com",
            "meetingDateTime": "2021-07-22T19:37:18.417+00:00",
            "meetingPlace": {
                "firstLine": "All sports Centre",
                "secondLine": "Queen's Road",
                "townCity": "Loughborough",
                "postCode": "LOUGHBOROUGH"
            },
            "otherSupervisors": "Other people involved",
            "family": {
                "parentList": {
                    "parents": [
                        {
                            "firstName": "The",
                            "lastName": "Parent",
                            "isTakingPart": false,
                            "parentUsername": "parent"
                        }
                    ]
                },
                "childList": {
                    "children": [
                        {
                            "firstName": "Bob",
                            "lastName": "Smith",
                            "receivesFreeSchoolMeals": false,
                            "isTakingPart": true
                        }
                    ]
                }
            }
        }
    ]
}
```

Note now that a non-null bookingRef is assigned. Subsequent changes (for example, the parent instead decides to take part) to this particular booking
__must__ be submitted under the bookingRef "tennis101_00:00_22-07-21_TP_20:42_22-07".

#### Booking reference format

The bookingRef is unique for this family and can be understood as follows:

```json
tennis101_00:00_22-07-21_TP_20:42_22-07
```

First is the uniqueID of the activity template, followed by the time and date the activity takes place, the initials of the 
parent who initiated the booking and then finally the time and date of the booking. The latter time and date of the booking is 
updated in all future booking changes. Furthermore, a bookingRef persists, even if the family decide not to take part at all.

What is important is to ensure that whenever a bookingRef is provided, that the __same__ bookingRef is submitted. If it was null 
on GET request, then it must be null on PUT request.

## Auto-lockout

All users can attempt to log in [3 times](./src/main/java/uk/org/breakthemould/listeners/AuthenticationFailureListener.java) 
within [3 hours](./src/main/java/uk/org/breakthemould/listeners/AuthenticationFailureListener.java) before their account 
is automatically locked. Users must wait [3 hours](./src/main/java/uk/org/breakthemould/listeners/AuthenticationFailureListener.java) after 
the last failed login attempt before they are allowed to attempt another log in.

## HTTP error responses

In general, whenever an error is caught, the backend will send a JSON response in the form:

```json
{
    "httpStatusCode": 403,
    "httpStatus": "FORBIDDEN",
    "reason": "FORBIDDEN",
    "message": "Log in to access this resource",
    "timestamp": "01-08-2021 11:45:42"
}
```

## API routing

OpenAPI (formerly Swagger) documentation [here](http://localhost:5000/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config#/) (run
the project first). This shows which endpoints have been implemented. Other endpoints in the works shown below.
The endpoints documented by OpenAPI may not work since JWT is missing.

Some terminology about activity related entities:

+ __Activity template__: record of activity name, description and URL (of website containing related info)
+ __Activity detail__:  activity template + meeting place, date, time and names of other coaches/supervisors
+ __Registering parent__: the parent who submits their personal data first

Activity booking register/participant list will be one-to-one/embedded with activity detail and thus identified by the activity detail
ID. Both entities above belong to one admin or staff user. Below are the provisional API routing.

```raml
#%RAML 1.0
---
title: BTM Activity Booking System
baseUri: http://localhost:3000/api/v1
version: v1
/users:
    description: All user entities (admin, staff, parent) on file
    get:
        description: Get a list of all users on file, sorted by ID (DONE)
    /byID:
        get:
        description: Get user by user ID
        queryParameters:
            id:
                description: Find user by database ID (DONE)
                type: number
                example: 2
    /admin:
        get:
            description: Get a list of all ADMIN users
        post:
            description: Submit new admin user with username and email address (DONE)
    /staff:
        get:
            description: Get a list of all STAFF users
        post:
            description: Submit new staff user with username and email address (DONE)
    /parent:
        get:
            description: Get a list of all PARENT users
        post:
            description: Submit new parent user with username and email address (DONE)
        delete:
            description: Removes a parent's record from the database (DONE)
    /child:
        post:
            description: Submit new child record(s) to the database (DONE)
        delete:
            description: Removes a child's record from the database (DONE)
    /registerParent:
        put:
            description: Update personal details of a registering parent (DONE)
    /otherParent:
        put:
            description: Update personal details of a non-registering (other) parent (DONE)
    /activateParentsAccount:
        post:
            description: Activate a parent or parents account after registration (DONE)
    /btmRep:
        get:
            description: Get assigned BTM representative of parent with username 
            provided (DONE)
        put:
            description: Update BTM representative of the parent with username 
            provided (DONE)
    /login:
        post:
            description: Send username and password at login page (with JSON web 
            token auth) (DONE)
    /toggleLock:
        put:
            description: Allow admin to lock/unlock other admin/staff/parent account, 
            and staff to lock/unlock a parent's account (DONE)
    /children:
        get:
            description: Retrieve the details of all children of the logged in 
            parent (DONE)
        put:
            description: Update the details of all children of the logged in 
            parent (DONE)
    /{username}/username:
        get:
            description: Find a user (with given username) (DONE)
        put:
            description: Update an existing user's (personal) details (DONE)
    /resetPassword:
        post:
            description: Send a request to reset password (DONE)
    /changePassword:
        post:
            description: Send a request to change the current password (DONE)
/activityTemplate:
    description: All activity templates on file
    get:
      description: get all templates on file, sorted by UniqueID (DONE)
    /byUniqueID:
         get:
             description: get template by uniqueID
             queryParameters:
                 uniqueId:
                     description: Find by activity uniqueID string field (DONE)
                     type: string
                     example: tennis101
    post:
        description: Request for new activity template (DONE)
    /{username}/owner:
        get:
            description: Find by activity template owner username (DONE)
    /{id}/database:
        get:
            description: Get by template database ID (DONE)
        put:
            description: Update template with ID (DONE)
/activityDetail:
    description: All activity details (template + meeting time/place) on file
        get:
            description: get all details on file, sorted by template uniqueID 
            followed by detail database ID (DONE)
        put:
            description: Update activity detail (DONE)
        post:
            description: Submit a new activity detail (DONE)
    /{username}:
        /organiserUsername:
            get:
                description: Find all activity details by activity organiser 
                username (DONE)
        /parentUsername:
            get:
                description: Find all activity details by parent (participant) 
                username (DONE)
        /{firstName}/{lastName}/child:
            get:
                description: Find all activity details by child (participant) 
                first and last name and parent username (DONE)
    /{uniqueID}/uniqueID:
            get:
                description: Find all activity details by template uniqueID (DONE)
    /participantList/{uniqueID}/{organiserUsername}:
        get:
            description: Get a list of participants for a given activity detail (DONE)
            queryParameters:
                 startDateTime:
                     description: Start dateTime
                     type: string
                     example: 2021-07-10T20:10
    /between:
        get:
            description: Retrieve all activity details (and their template) 
            between two given dates and times; activities ordered chronologically (DONE)
            queryParameters:
                 startDateTime:
                     description: Start dateTime
                     type: string
                     example: 2021-07-10T20:10
                 endDateTime:
                     description: End dateTime
                     type: string
                     example: 2021-08-22T20:10
    /withBookings:
        get:
            description: Retrieve all activity details and bookings between two given 
            dates and times for the family of the logged in parent (DONE)
            queryParameters:
                 startDateTime:
                     description: Start dateTime
                     type: string
                     example: 2021-07-10T20:10
                 endDateTime:
                     description: End dateTime
                     type: string
                     example: 2021-08-22T20:10
        put:
            description: Update all activity details and bookings between two given 
            dates and times for the family of the logged in parent (DONE)
    /{detailId}/database:
        get:
            description: Get activity detail (by database ID) (DONE)
```

## How (response) lists are ordered

The backend is configured to sort lists of database records whenever it returns a list in the response. These lists manifest as JSON arrays and can be 
overridden by the client at any time.

Lists are typically sorted in alphabetical order. Generally, the ordering of lists which are part of PUT requests are not altered when they are returned 
in the response. For more info on how specific lists are ordered, please consult the OpenAPI documentation.

## Exceptions and HTTP response codes

The following table summarises the types of HTTP respond codes and messages sent whenever an exception (predicted run-time errors) is raised.
They are defined in [APIExceptionHandler.java](/src/main/java/uk/org/breakthemould/exception/APIExceptionHandler.java). Note that not all exceptions
should be treated as fatal errors. Methods are usually made available to pick up the pieces (in the Java catch or finally block) and allow 
the backend to continue running.

Error codes of the type 4xx are client-side errors (the frontend or through POSTMAN).
Codes of the type 5xx are typically server (backend) side errors and a require backend code bug fix. All error codes adhere to the standard HTTP protocol.

| HTTP code | Default response phrase                               | Comments                                                                |
|-----------|-------------------------------------------------------|-------------------------------------------------------------------------|
| 400       | Your account has been disabled.                       | User attempted to login when their is disabled                          |
| 400       | URL parameter error                                   | Endpoint URL parameter not recognised or missing                        |
| 400       | Start-date must occur before end-date                 | Activity detail endpoint URL date-time parameter is the wrong way round |
| 400       | Username or password are missing or incorrect.        | Login details submitted do not match those on file                      |
| 401       | Username or password are missing or incorrect.        | Authenticated user not authorised to access given method                |
| 423       | Your account has been locked.                         | Caused by multiple failed login attempts. See "Auto-lockout" above      |
| 405       | The request method is not allowed. '%s' was expected. | Endpoint URL is probably OK but HTTP method (GET, POST) used is not     |
| 500       | File processing error.                                | Problem related to backend and database mapping methods                 |
| 500       | A server error occurred when processing the request.  | Raised whenever __all__ other error codes (4xx or 5xx) do not apply     |

The majority of errors are categorised under 400 ("Bad request"), 422 ("Unprocessable Entity"), 403 ("Forbidden") or 404 ("Not found") and
return a response message (phrase) with more specific information related to the problem, and do not have a default response phrase.

Codes provide a standard way of sending status reports to the client and there is some debate about which codes to apply to specific error conditions.
Not all errors are fatal. It might be that a 404 is raised when the database is not supposed to have a record of an activity or user.

Some 4xx errors (shown below) are generalised error responses and categorise/group errors which may involve more than one error condition. 
Some typical use-cases are given below.

| HTTP code | Example use-case                                                  | Comments                                                                   |
|-----------|-------------------------------------------------------------------|----------------------------------------------------------------------------|
| 404       | Parent, child or activity template not found                      | Arises when the backend cannot find a database record                      |
| 403       | Applies to database access method (all user types and activities) | User not permitted (is forbidden) to access other users' resources/methods |
| 400       | Cannot duplicate usernames for new users                          | Admin/Staff user attempting to register a username that already exists     |
| 400       | Activity template uniqueID already in use                         | Backend cannot build new activity template with an existing uniqueID       |
| 400       | User database record not found                                    | Internal backend logic attempting to find user database entity             |
| 422       | Backend received an invalid URL parameter type                    | Endpoint URL parameter received but not formatted correctly                |
| 422       | Backend received an invalid JSON payload                          | JSON payload received but not formatted correctly/missing field(s)         |
| 422       | Personal data missing during parent registration                  | JSON payload received but missing required fields                          |


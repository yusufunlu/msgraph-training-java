// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.

package graphtutorial;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Calendar;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.graph.auth.confidentialClient.AuthorizationCodeProvider;
import com.microsoft.graph.auth.confidentialClient.ClientCredentialProvider;
import com.microsoft.graph.auth.enums.NationalCloud;
import com.microsoft.graph.auth.publicClient.UsernamePasswordProvider;
import com.microsoft.graph.core.DefaultClientConfig;
import com.microsoft.graph.http.IHttpProvider;
import com.microsoft.graph.httpcore.HttpClients;
import com.microsoft.graph.httpcore.ICoreAuthenticationProvider;
import com.microsoft.graph.logger.DefaultLogger;
import com.microsoft.graph.logger.LoggerLevel;
import com.microsoft.graph.models.extensions.*;
import com.microsoft.graph.models.generated.AccessLevel;
import com.microsoft.graph.models.generated.AttendeeType;
import com.microsoft.graph.models.generated.BodyType;
import com.microsoft.graph.models.generated.MeetingCapabilities;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.microsoft.graph.requests.extensions.IEventCollectionPage;
import com.microsoft.graph.requests.extensions.ITeamCollectionWithReferencesPage;
import com.microsoft.graph.serializer.DefaultSerializer;
import okhttp3.OkHttpClient;
import org.joda.time.DateTime;
import com.microsoft.graph.serializer.DefaultSerializer;

import static com.microsoft.graph.models.generated.LobbyBypassScope.EVERYONE;

/**
 * Graph
 */
public class Graph {

    private static IGraphServiceClient graphClient = null;
    private static SimpleAuthProvider authProvider = null;

    private static final String CLIENT_ID = "d2dff56c-029c-42f4-b603-f0c272fb8e11";
    private static final List<String> SCOPES = Arrays.asList("https://graph.microsoft.com/user.read","https://graph.microsoft.com/Mail.ReadWrite");
    private static final String AUTHORIZATION_CODE = "";
    private static final String REDIRECT_URL = "";
    private static final NationalCloud NATIONAL_CLOUD = NationalCloud.Global;
    private static final String TENANT = "39d77147-bbfe-4e85-8b97-41ae6698b503";
    private static final String CLIENT_SECRET = "DaAu~wmb_vD9P1gSWoFHa6iIm8_0-8Jl1p";
    private static final String TENANT_GUID = "";
    private static final String USERNAME = "yusufu@dealroomevents202.onmicrosoft.com";
    private static final String PASSWORD = "eyJ0eXAiOiJKV1QiLCJub25jZSI6ImduR1FaVzc4THBXdkp4T3FFNjdYZlZieGUwLTJJTmpwUm5nSnRIa3g0aUEiLCJhbGciOiJSUzI1NiIsIng1dCI6ImtnMkxZczJUMENUaklmajRydDZKSXluZW4zOCIsImtpZCI6ImtnMkxZczJUMENUaklmajRydDZKSXluZW4zOCJ9.eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC8zOWQ3NzE0Ny1iYmZlLTRlODUtOGI5Ny00MWFlNjY5OGI1MDMvIiwiaWF0IjoxNjAzMDk3MzA1LCJuYmYiOjE2MDMwOTczMDUsImV4cCI6MTYwMzEwMTIwNSwiYWNjdCI6MCwiYWNyIjoiMSIsImFpbyI6IkUyUmdZSGpmSjdqc2JybXljT2ZQcnVVbGhZOFk3emhkT0tseHlEL2d4clF5d1ZWZU03a0EiLCJhbXIiOlsicHdkIl0sImFwcF9kaXNwbGF5bmFtZSI6IkphdmEgR3JhcGggVHV0b3JpYWwiLCJhcHBpZCI6ImQyZGZmNTZjLTAyOWMtNDJmNC1iNjAzLWYwYzI3MmZiOGUxMSIsImFwcGlkYWNyIjoiMSIsImZhbWlseV9uYW1lIjoidW5sdSIsImdpdmVuX25hbWUiOiJ5dXN1ZiIsImlkdHlwIjoidXNlciIsImlwYWRkciI6Ijg4LjI0My4xOTkuMTYwIiwibmFtZSI6Inl1c3VmdSIsIm9pZCI6IjYxY2ZkNTJhLWU5YjEtNDVkOS1hY2Q0LTJkOWVmZTQ4ZjJmZSIsInBsYXRmIjoiMTQiLCJwdWlkIjoiMTAwMzIwMDBFRTZBMzUyNyIsInJoIjoiMC5BQUFBUjNIWE9mNjdoVTZMbDBHdVpwaTFBMnoxMzlLY0F2UkN0Z1B3d25MN2poRmdBUFkuIiwic2NwIjoiTm90ZXMuQ3JlYXRlIE5vdGVzLlJlYWQgTm90ZXMuUmVhZC5BbGwgTm90ZXMuUmVhZFdyaXRlIE5vdGVzLlJlYWRXcml0ZS5BbGwgTm90ZXMuUmVhZFdyaXRlLkNyZWF0ZWRCeUFwcCBPbmxpbmVNZWV0aW5ncy5SZWFkIE9ubGluZU1lZXRpbmdzLlJlYWRXcml0ZSBUZWFtLkNyZWF0ZSBUZWFtLlJlYWRCYXNpYy5BbGwgVGVhbXNBcHAuUmVhZCBUZWFtc0FwcC5SZWFkLkFsbCBUZWFtc0FwcC5SZWFkV3JpdGUgVGVhbXNBcHAuUmVhZFdyaXRlLkFsbCBVc2VyLlJlYWQgcHJvZmlsZSBvcGVuaWQgZW1haWwiLCJzdWIiOiI4RVdvU0hIVGdvTDI1NksyVlpDejAxNTI5Mm1qUmY2dnFDLVFKYXBWUUMwIiwidGVuYW50X3JlZ2lvbl9zY29wZSI6IkVVIiwidGlkIjoiMzlkNzcxNDctYmJmZS00ZTg1LThiOTctNDFhZTY2OThiNTAzIiwidW5pcXVlX25hbWUiOiJ5dXN1ZnVAZGVhbHJvb21ldmVudHMyMDIub25taWNyb3NvZnQuY29tIiwidXBuIjoieXVzdWZ1QGRlYWxyb29tZXZlbnRzMjAyLm9ubWljcm9zb2Z0LmNvbSIsInV0aSI6IkhTV1laVk00MzAyLXFFVlBpVVNLQUEiLCJ2ZXIiOiIxLjAiLCJ3aWRzIjpbImJhZjM3YjNhLTYxMGUtNDVkYS05ZTYyLWQ5ZDFlNWU4OTE0YiIsIjYyZTkwMzk0LTY5ZjUtNDIzNy05MTkwLTAxMjE3NzE0NWUxMCIsImI3OWZiZjRkLTNlZjktNDY4OS04MTQzLTc2YjE5NGU4NTUwOSJdLCJ4bXNfc3QiOnsic3ViIjoiMVRzblYtNUxoVVZTeHhwdHZLQ09KYWVFbmFjaGRMR21ieGVrTTF0VE0xRSJ9LCJ4bXNfdGNkdCI6MTYwMjUxMTk4OH0.SrMCRCKVXSXU5dUgd-KhGVLdVgyaDellSSmAMrlIvCRmZdg6xqmfSpFxwAEOqHEAt5opS_Yqd0IPeKticpG_KPmksP5tkoflGNmIcpjwYBwKCfx2jvUUj1Qc2BYSFp1CilSSVbCL2tQJyRBzmoBaf_5RpQ_Y6d5ixR3yKWQ5vwM6ijAC36IaRR_5vWh3wwKIsJjEj5VLpAN0YXNvO7_JEB8aos1Kew4_NGiSMTJSLyGS8ZZ_GTiWOlYoBCKBHOeiKp5O7sriKrsJsl9VxKAqor2T8lzhNS6wpjBs72nY0FvfMC5pp00l8FwIakaQcZXurD2V2N6QdLTYp0zIvTT3Mw";


    private ICoreAuthenticationProvider createUsernamePasswordProvider(){
        UsernamePasswordProvider usernamePasswordProvider = new UsernamePasswordProvider(CLIENT_ID, SCOPES, USERNAME, PASSWORD, NationalCloud.Global, TENANT, CLIENT_SECRET);
        return usernamePasswordProvider;
    }

    private ICoreAuthenticationProvider createAuthorizationCodeProvider(){
        AuthorizationCodeProvider authorizationCodeProvider = new AuthorizationCodeProvider(CLIENT_ID, SCOPES, AUTHORIZATION_CODE, REDIRECT_URL, NATIONAL_CLOUD, TENANT, CLIENT_SECRET);
        return authorizationCodeProvider;
    }

    private ICoreAuthenticationProvider createClientCredentialProvider(){
        ClientCredentialProvider clientCredentialProvider = new ClientCredentialProvider(CLIENT_ID, SCOPES, CLIENT_SECRET, TENANT_GUID, NATIONAL_CLOUD);
        return clientCredentialProvider;
    }

    private static SimpleAuthProvider createSimpleAuthProvider(String accessToken){
        authProvider = new SimpleAuthProvider(accessToken);
        return authProvider;
    }


    private static void ensureGraphClient(String accessToken) {

        if (graphClient == null) {
            // Create the auth provider
            authProvider = createSimpleAuthProvider(accessToken);
            // Create default logger to only log errors
            DefaultLogger logger = new DefaultLogger();
            logger.setLoggingLevel(LoggerLevel.DEBUG);

            // Build a Graph client
            graphClient = GraphServiceClient.builder()
                .authenticationProvider(authProvider)
                .logger(logger)
                .buildClient();

            graphClient.setServiceRoot("https://graph.microsoft.com/beta");
        }
    }



    public static User getUser(String accessToken) {
        ensureGraphClient(accessToken);

        // GET /me to get authenticated user
        User me = graphClient
            .me()
            .buildRequest()
            .get();

        return me;
    }

    public static OnlineMeeting createOnlineMeeting(String accessToken) {
        ensureGraphClient(accessToken);

        OnlineMeeting onlineMeeting = new OnlineMeeting();

        TimeZone zone = TimeZone.getTimeZone("GMT");
        onlineMeeting.startDateTime = Calendar.getInstance(zone);
        onlineMeeting.endDateTime = Calendar.getInstance(zone);
        //DateTimeFormatter dateTimeFormatter = DateTimeFormatter.of

        Date date = new Date();
        date.setHours(date.getHours()+1);
        onlineMeeting.startDateTime.setTime(date);

        Date date2 = new Date();
        date2.setHours(date2.getHours()+3);
        onlineMeeting.endDateTime.setTime(date2);

        onlineMeeting.startDateTime.setTimeZone(zone);

        LobbyBypassSettings lobbyBypassSettings = new LobbyBypassSettings();
        lobbyBypassSettings.scope = EVERYONE;
        lobbyBypassSettings.isDialInBypassEnabled = true;

        onlineMeeting.lobbyBypassSettings = lobbyBypassSettings;

        onlineMeeting.subject = "User Token Meeting";
        Attendee attendee1 = new Attendee();
        EmailAddress emailAddress = new EmailAddress();
        emailAddress.name = "Yusuf Ünlü";
        emailAddress.address = "unlu.yufus@gmail.com";
        attendee1.emailAddress = emailAddress;

        MeetingParticipantInfo meetingParticipantInfo = new MeetingParticipantInfo();


        MeetingParticipants meetingParticipants = new MeetingParticipants();
        LinkedList<MeetingParticipantInfo> attendees = new LinkedList<MeetingParticipantInfo>();
        MeetingParticipantInfo targets = new MeetingParticipantInfo();
        IdentitySet identity = new IdentitySet();
        Identity user = new Identity();
        user.displayName = "Yusuf Ünlü";
        user.id = "61cfd52a-e9b1-45d9-acd4-2d9efe48f2fe";
        identity.user = user;
        targets.identity = identity;
        attendees.add(targets);
        meetingParticipants.attendees = attendees;

        onlineMeeting.accessLevel = AccessLevel.EVERYONE;
        onlineMeeting.participants = meetingParticipants;
        OnlineMeeting createdOnlineMeeting = graphClient.me().onlineMeetings()
                .buildRequest()
                .post(onlineMeeting);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(createdOnlineMeeting.getRawObject());
        System.out.println("Created Online Meeting: " + json);

        return createdOnlineMeeting;

    }

    public static List<Team> myJoinedTeams(String accessToken){
        ensureGraphClient(accessToken);
        ITeamCollectionWithReferencesPage joinedTeams = graphClient.me().joinedTeams()
                .buildRequest()
                .get();
        List<Team> myTeams = joinedTeams.getCurrentPage();
        return myTeams;
    }

    // <GetEventsSnippet>
    public static List<Event> getEvents(String accessToken) {
        ensureGraphClient(accessToken);

        // Use QueryOption to specify the $orderby query parameter
        final List<Option> options = new LinkedList<Option>();
        // Sort results by createdDateTime, get newest first
        options.add(new QueryOption("orderby", "createdDateTime DESC"));

        // GET /me/events
        IEventCollectionPage eventPage = graphClient
            .me()
            .events()
            .buildRequest(options)
            .select("subject,body,bodyPreview,organizer,attendees, start,end,location")
            .get();

        return eventPage.getCurrentPage();
    }
    // </GetEventsSnippet>

    public static void createEvent(String accessToken) {
        ensureGraphClient(accessToken);

        Event event = createEventObject();
        graphClient.me().events()
                .buildRequest()
                .post(event);
    }

    private static Event createEventObject() {
        Event event = new Event();
        event.subject = "Microsoft Graph SDK Tartışması";
        // set start time to now
        DateTimeTimeZone start = new DateTimeTimeZone();
        start.dateTime = DateTime.now().toString();
        event.start = start;

        // and end in 1 hr
        DateTimeTimeZone end = new DateTimeTimeZone();
        end.dateTime = DateTime.now().plusHours(1).toString();
        event.end = end;

        // set the timezone
        start.timeZone = end.timeZone = "UTC";

        // set a location
        Location location = new Location();
        location.displayName = "Yusuf'un evi";
        event.location = location;

        // add attendees
        Attendee attendee = new Attendee();
        attendee.type = AttendeeType.REQUIRED;
        attendee.emailAddress = new EmailAddress();
        attendee.emailAddress.address = "yusuf.unlu@dealroomevents.com";
        event.attendees = Collections.singletonList(attendee);

        // add a msg
        ItemBody msg = new ItemBody();
        msg.content = "Microsoft Graph SDK planlaması";
        msg.contentType = BodyType.TEXT;
        event.body = msg;

        return event;
    }

    private static Event createEventObject2() {

        Event event = new Event();
        event.subject = "Let's go for lunch";
        ItemBody body = new ItemBody();
        body.contentType = BodyType.HTML;
        body.content = "Does noon work for you?";
        event.body = body;

        // set start time to now
        DateTimeTimeZone start = new DateTimeTimeZone();
        start.dateTime = DateTime.now().toString();
        event.start = start;
        // and end in 1 hr
        DateTimeTimeZone end = new DateTimeTimeZone();
        end.dateTime = DateTime.now().plusHours(1).toString();
        event.end = end;

        Location location = new Location();
        location.displayName = "Harry's Bar";
        event.location = location;
        LinkedList<Attendee> attendeesList = new LinkedList<Attendee>();
        Attendee attendees = new Attendee();
        EmailAddress emailAddress = new EmailAddress();
        emailAddress.address =  "yusuf.unlu@dealroomevents.com";
        emailAddress.name = "Samantha Booth";
        attendees.emailAddress = emailAddress;
        attendees.type = AttendeeType.REQUIRED;
        attendeesList.add(attendees);
        event.attendees = attendeesList;
        event.transactionId = "7E163156-7762-4BEB-A1C6-729EA81755A7";
        return event;
    }
}
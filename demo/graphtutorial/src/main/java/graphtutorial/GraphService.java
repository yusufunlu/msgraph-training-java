// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.

package graphtutorial;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.graph.auth.confidentialClient.AuthorizationCodeProvider;
import com.microsoft.graph.auth.confidentialClient.ClientCredentialProvider;
import com.microsoft.graph.auth.enums.NationalCloud;
import com.microsoft.graph.auth.publicClient.UsernamePasswordProvider;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.httpcore.HttpClients;
import com.microsoft.graph.httpcore.ICoreAuthenticationProvider;
import com.microsoft.graph.logger.LoggerLevel;
import com.microsoft.graph.models.extensions.*;
import com.microsoft.graph.models.generated.AccessLevel;
import com.microsoft.graph.models.generated.AttendeeType;
import com.microsoft.graph.models.generated.BodyType;
import com.microsoft.graph.models.generated.OnlineMeetingPresenters;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.microsoft.graph.requests.extensions.IEventCollectionPage;
import com.microsoft.graph.requests.extensions.ITeamCollectionWithReferencesPage;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.*;

import static com.microsoft.graph.models.generated.LobbyBypassScope.EVERYONE;

/**
 * Graph
 */
public class GraphService {

    private static final List<String> SCOPES = Arrays.asList("https://graph.microsoft.com/.default");
    private static IGraphServiceClient graphClient = null;
    private static SimpleAuthProvider authProvider = null;
    private static OkHttpClient okHttpClient;
    private final Properties oAuthProperties;
    private final IAuthenticationProvider iAuthenticationProvider;
    private final ICoreAuthenticationProvider iCoreAuthenticationProvider;
    private User me;

    public GraphService(Properties oAuthProperties) {
        this.oAuthProperties = oAuthProperties;
        createOkHttpClient();
        iAuthenticationProvider = createUsernamePasswordProvider();
        iCoreAuthenticationProvider = createUsernamePasswordProvider();
    }

    private UsernamePasswordProvider createUsernamePasswordProvider() {
        UsernamePasswordProvider usernamePasswordProvider = new UsernamePasswordProvider(
                oAuthProperties.getProperty("CLIENT_ID"),
                SCOPES,
                oAuthProperties.getProperty("USERNAME"),
                oAuthProperties.getProperty("PASSWORD"),
                NationalCloud.Global,
                oAuthProperties.getProperty("TENANT"),
                oAuthProperties.getProperty("CLIENT_SECRET"));
        return usernamePasswordProvider;
    }


    private AuthorizationCodeProvider createAuthorizationCodeProvider() {
        AuthorizationCodeProvider authorizationCodeProvider = new AuthorizationCodeProvider(
                oAuthProperties.getProperty("CLIENT_ID"),
                SCOPES,
                oAuthProperties.getProperty("AUTHORIZATION_CODE"),
                oAuthProperties.getProperty("REDIRECT_URL"),
                NationalCloud.Global,
                oAuthProperties.getProperty("TENANT"),
                oAuthProperties.getProperty("CLIENT_SECRET"));
        return authorizationCodeProvider;
    }

    private ClientCredentialProvider createClientCredentialProvider() {
        ClientCredentialProvider clientCredentialProvider = new ClientCredentialProvider(
                oAuthProperties.getProperty("CLIENT_ID"),
                SCOPES,
                oAuthProperties.getProperty("CLIENT_SECRET"),
                oAuthProperties.getProperty("TENANT"),
                NationalCloud.Global);
        return clientCredentialProvider;
    }

    private SimpleAuthProvider createSimpleAuthProvider(String accessToken) {
        authProvider = new SimpleAuthProvider(accessToken);
        return authProvider;
    }

    private OkHttpClient createOkHttpClient() {

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        okHttpClient = HttpClients.createDefault(iCoreAuthenticationProvider)
                .newBuilder()
                .addInterceptor(loggingInterceptor)
                //.addInterceptor(new LoggingInterceptor())
                .followSslRedirects(false)
                .build();

        return okHttpClient;
    }


    private void initGraphClient() {

        if (graphClient == null) {
            graphClient = GraphServiceClient
                    .builder()
                    .authenticationProvider(iAuthenticationProvider)
                    .buildClient();
            graphClient.getLogger().setLoggingLevel(LoggerLevel.DEBUG);
            graphClient.setServiceRoot("https://graph.microsoft.com/beta");
        }
    }


    public User getUserSimpleWay() {
        initGraphClient();

        User me = graphClient
                .me()
                .buildRequest()
                .get();
        return me;
    }

    public void getUserWithCallback() {
        initGraphClient();
        graphClient
                .me()
                .buildRequest()
                .get(new ICallback<User>() {
                    @Override
                    public void success(User user) {
                        me = user;
                        System.out.println("User: " + user.getRawObject().toString());
                    }

                    @Override
                    public void failure(ClientException ex) {
                        System.out.println("User Exception: " + ex.getLocalizedMessage());
                    }
                });
    }

    public void getUserWithOkhttpclient() {

        Request request = new Request.Builder().url("https://graph.microsoft.com/beta/me/").build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String responseBody = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Your processing with the response body
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });

    }


    public OnlineMeeting createOnlineMeeting(String accessToken) {
        initGraphClient();

        OnlineMeeting onlineMeeting = new OnlineMeeting();

        TimeZone zone = TimeZone.getTimeZone("GMT");
        onlineMeeting.startDateTime = Calendar.getInstance(zone);
        onlineMeeting.endDateTime = Calendar.getInstance(zone);
        //DateTimeFormatter dateTimeFormatter = DateTimeFormatter.of

        Date date = new Date();
        date.setHours(date.getHours() + 1);
        onlineMeeting.startDateTime.setTime(date);

        Date date2 = new Date();
        date2.setHours(date2.getHours() + 3);
        onlineMeeting.endDateTime.setTime(date2);

        onlineMeeting.startDateTime.setTimeZone(zone);

        LobbyBypassSettings lobbyBypassSettings = new LobbyBypassSettings();
        lobbyBypassSettings.scope = EVERYONE;
        lobbyBypassSettings.isDialInBypassEnabled = true;

        onlineMeeting.lobbyBypassSettings = lobbyBypassSettings;
        onlineMeeting.allowedPresenters = OnlineMeetingPresenters.EVERYONE;
        onlineMeeting.subject = "User Token Meeting";
        Attendee attendee1 = new Attendee();
        EmailAddress emailAddress = new EmailAddress();
        emailAddress.name = "Yusuf Ünlü1";
        emailAddress.address = "unlu.yufus@gmail.com";
        attendee1.emailAddress = emailAddress;


        MeetingParticipants meetingParticipants = new MeetingParticipants();
        LinkedList<MeetingParticipantInfo> attendees = new LinkedList<MeetingParticipantInfo>();
        MeetingParticipantInfo targets = new MeetingParticipantInfo();
        IdentitySet identitySet = new IdentitySet();
        Identity identity = new Identity();
        identity.displayName = "Yusuf Ünlü2";
        identity.id = "61cfd55a-e9b1-45d9-acd4-2d9efe48f2fe";
        identitySet.user = identity;
        targets.identity = identitySet;
        attendees.add(targets);
        meetingParticipants.attendees = attendees;

        onlineMeeting.accessLevel = AccessLevel.EVERYONE;
        onlineMeeting.participants = meetingParticipants;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        OnlineMeeting createdOnlineMeeting = new OnlineMeeting();
        try {
            //System.out.println("Online Meeting: " + gson.toJson(onlineMeeting));
            createdOnlineMeeting = graphClient.me().onlineMeetings()
                    .buildRequest()
                    .post(onlineMeeting);
        } catch (Exception exception) {
            System.out.println("Exception: " + exception.getLocalizedMessage());
        }

        String json = gson.toJson(createdOnlineMeeting.getRawObject());
        System.out.println("Created Online Meeting: " + json);

        return createdOnlineMeeting;

    }

    public List<Team> myJoinedTeams(String accessToken) {
        initGraphClient();
        ITeamCollectionWithReferencesPage joinedTeams = graphClient.me().joinedTeams()
                .buildRequest()
                .get();
        List<Team> myTeams = joinedTeams.getCurrentPage();
        return myTeams;
    }

    // <GetEventsSnippet>
    public List<Event> getEvents(String accessToken) {
        initGraphClient();

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

    public void createEvent(String accessToken) {
        initGraphClient();

        Event event = createEventObject();
        graphClient.me().events()
                .buildRequest()
                .post(event);
    }

    private Event createEventObject() {
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

    private Event createEventObject2() {

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
        emailAddress.address = "yusuf.unlu@dealroomevents.com";
        emailAddress.name = "Samantha Booth";
        attendees.emailAddress = emailAddress;
        attendees.type = AttendeeType.REQUIRED;
        attendeesList.add(attendees);
        event.attendees = attendeesList;
        event.transactionId = "7E163156-7762-4BEB-A1C6-729EA81755A7";
        return event;
    }
}
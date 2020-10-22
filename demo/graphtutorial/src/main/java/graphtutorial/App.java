// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package graphtutorial;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Properties;

import com.microsoft.graph.models.extensions.DateTimeTimeZone;
import com.microsoft.graph.models.extensions.Event;
import com.microsoft.graph.models.extensions.Team;

/**
 * Graph Tutorial
 *
 */
public class App {
    private static GraphService graphService;
    public static void main(String[] args) {
        System.out.println("Java Graph Tutorial");
        System.out.println();

        // <LoadSettingsSnippet>
        // Load OAuth settings
        final Properties oAuthProperties = new Properties();
        try {
            oAuthProperties.load(App.class.getResourceAsStream("oAuth.properties"));
        } catch (IOException e) {
            System.out.println("Unable to read OAuth configuration. Make sure you have a properly formatted oAuth.properties file. See README for details.");
            return;
        }

        final String appId = oAuthProperties.getProperty("app.id");
        final String[] appScopes = oAuthProperties.getProperty("app.scopes").split(",");
        // </LoadSettingsSnippet>

        graphService = new GraphService(oAuthProperties);

        // Get an access token
        //Authentication.initialize(appId);
        //final String accessToken = Authentication.getUserAccessToken(appScopes);
        final String accessToken = "";
        System.out.println("accessToken: " + accessToken);
        int choice = 0;

        System.out.println("Please choose one of the following options:");
        System.out.println("0. Greet the user");
        System.out.println("1. Display access token");
        System.out.println("2. List calendar events");
        System.out.println("3. create calendar events");
        System.out.println("4. create online meeting");
        System.out.println("5. My joined teams");


        // Process user choice
        switch(choice) {
            case 0:
                // Greet the user
                graphService.getUser(accessToken);
                //System.out.println("Welcome " + user.displayName);
                //System.out.println("Welcome " + user.toString());
                break;
            case 1:
                // Display access token
                System.out.println("Access token: " + accessToken);
                break;
            case 2:
                // List the calendar
                listCalendarEvents(accessToken);
                break;
            case 3:
                // List the calendar
                graphService.createEvent(accessToken);
                break;
            case 4:
                // create online meeting
                graphService.createOnlineMeeting(accessToken);
                break;
            case 5:
                List<Team> teamList = graphService.myJoinedTeams(accessToken);
                System.out.println("My Joined Teams: "+ teamList.get(0).getRawObject());
                break;
            default:
                System.out.println("Invalid choice");
        }
    }

    private static String formatDateTimeTimeZone(DateTimeTimeZone date) {
        LocalDateTime dateTime = LocalDateTime.parse(date.dateTime);

        return dateTime.format(
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)) +
            " (" + date.timeZone + ")";
    }

    private static void listCalendarEvents(String accessToken) {
        // Get the user's events
        List<Event> events = graphService.getEvents(accessToken);

        System.out.println("Events:");

        for (Event event : events) {
            System.out.println("Subject: " + event.subject);
            System.out.println("  Organizer: " + event.organizer.emailAddress.name);
            System.out.println("  Start: " + formatDateTimeTimeZone(event.start));
            System.out.println("  End: " + formatDateTimeTimeZone(event.end));
        }

        System.out.println();
    }
}
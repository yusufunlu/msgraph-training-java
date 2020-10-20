// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package graphtutorial;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import com.google.gson.Gson;
import com.microsoft.graph.models.extensions.DateTimeTimeZone;
import com.microsoft.graph.models.extensions.Event;
import com.microsoft.graph.models.extensions.Team;
import com.microsoft.graph.models.extensions.User;

/**
 * Graph Tutorial
 *
 */
public class App {
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

        // Get an access token
        Authentication.initialize(appId);
        //final String accessToken = Authentication.getUserAccessToken(appScopes);
        final String accessToken = "eyJ0eXAiOiJKV1QiLCJub25jZSI6Im05OC0yd3pCbEZZV2FDTERPTW4wWFRJZThHSlZ1OTFQaWlzUzV6eGJsMkUiLCJhbGciOiJSUzI1NiIsIng1dCI6ImtnMkxZczJUMENUaklmajRydDZKSXluZW4zOCIsImtpZCI6ImtnMkxZczJUMENUaklmajRydDZKSXluZW4zOCJ9.eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC8zOWQ3NzE0Ny1iYmZlLTRlODUtOGI5Ny00MWFlNjY5OGI1MDMvIiwiaWF0IjoxNjAzMTQ0MTA0LCJuYmYiOjE2MDMxNDQxMDQsImV4cCI6MTYwMzE0ODAwNCwiYWNjdCI6MCwiYWNyIjoiMSIsImFpbyI6IkUyUmdZTWpXTmVkOGtWeWFXTnBRTXV0MGRmYzExc012RW5kV255bTQ5REUxaU1GOHdYOEEiLCJhbXIiOlsicHdkIl0sImFwcF9kaXNwbGF5bmFtZSI6IkphdmEgR3JhcGggVHV0b3JpYWwiLCJhcHBpZCI6ImQyZGZmNTZjLTAyOWMtNDJmNC1iNjAzLWYwYzI3MmZiOGUxMSIsImFwcGlkYWNyIjoiMSIsImZhbWlseV9uYW1lIjoidW5sdSIsImdpdmVuX25hbWUiOiJ5dXN1ZiIsImlkdHlwIjoidXNlciIsImlwYWRkciI6Ijg4LjI0My4xOTkuMTYwIiwibmFtZSI6Inl1c3VmdSIsIm9pZCI6IjYxY2ZkNTJhLWU5YjEtNDVkOS1hY2Q0LTJkOWVmZTQ4ZjJmZSIsInBsYXRmIjoiMTQiLCJwdWlkIjoiMTAwMzIwMDBFRTZBMzUyNyIsInJoIjoiMC5BQUFBUjNIWE9mNjdoVTZMbDBHdVpwaTFBMnoxMzlLY0F2UkN0Z1B3d25MN2poRmdBUFkuIiwic2NwIjoiTm90ZXMuQ3JlYXRlIE5vdGVzLlJlYWQgTm90ZXMuUmVhZC5BbGwgTm90ZXMuUmVhZFdyaXRlIE5vdGVzLlJlYWRXcml0ZS5BbGwgTm90ZXMuUmVhZFdyaXRlLkNyZWF0ZWRCeUFwcCBPbmxpbmVNZWV0aW5ncy5SZWFkIE9ubGluZU1lZXRpbmdzLlJlYWRXcml0ZSBUZWFtLkNyZWF0ZSBUZWFtLlJlYWRCYXNpYy5BbGwgVGVhbXNBcHAuUmVhZCBUZWFtc0FwcC5SZWFkLkFsbCBUZWFtc0FwcC5SZWFkV3JpdGUgVGVhbXNBcHAuUmVhZFdyaXRlLkFsbCBVc2VyLlJlYWQgcHJvZmlsZSBvcGVuaWQgZW1haWwiLCJzdWIiOiI4RVdvU0hIVGdvTDI1NksyVlpDejAxNTI5Mm1qUmY2dnFDLVFKYXBWUUMwIiwidGVuYW50X3JlZ2lvbl9zY29wZSI6IkVVIiwidGlkIjoiMzlkNzcxNDctYmJmZS00ZTg1LThiOTctNDFhZTY2OThiNTAzIiwidW5pcXVlX25hbWUiOiJ5dXN1ZnVAZGVhbHJvb21ldmVudHMyMDIub25taWNyb3NvZnQuY29tIiwidXBuIjoieXVzdWZ1QGRlYWxyb29tZXZlbnRzMjAyLm9ubWljcm9zb2Z0LmNvbSIsInV0aSI6IjBKeWxtLTRFeTBxanZaWnpjbk9zQUEiLCJ2ZXIiOiIxLjAiLCJ3aWRzIjpbImJhZjM3YjNhLTYxMGUtNDVkYS05ZTYyLWQ5ZDFlNWU4OTE0YiIsIjYyZTkwMzk0LTY5ZjUtNDIzNy05MTkwLTAxMjE3NzE0NWUxMCIsImI3OWZiZjRkLTNlZjktNDY4OS04MTQzLTc2YjE5NGU4NTUwOSJdLCJ4bXNfc3QiOnsic3ViIjoiMVRzblYtNUxoVVZTeHhwdHZLQ09KYWVFbmFjaGRMR21ieGVrTTF0VE0xRSJ9LCJ4bXNfdGNkdCI6MTYwMjUxMTk4OH0.POca7TYuZ7JYFR4IkpmGTRh4ERgbHcNRckt-wF1-9f9HZbjZXeh8xzJM2fr1Q_iRSVI6xPQHUy2hwVzFkBqR-_K2rf04Fqj1JPs2Lzbaz4gAffgBNpUZt-8_9u74r8l01os48Ew3-ZxGjq7nvHw6dp9qS6J-59e8itVAn5RGgvCMhHwUYw-8evXD1ylSh1GcBqM2F6UJVg5HUk-FhHDN4Gy60IrCWxZgoSzrmd6RgG40frLkjsTtXHvGRkSVlb5WTuDNMd6doXqUQ8eUkjlVcX-pUMZpAu1rdgQ27a8TqCimfeUUZvG2PM1cmAIDLr7kAEvZXeK51LoRiFzJcl31wQ";
        System.out.println("accessToken: " + accessToken);
        int choice = 4;

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
                User user = Graph.getUser(accessToken);
                System.out.println("Welcome " + user.displayName);
                System.out.println("Welcome " + user.toString());
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
                Graph.createEvent(accessToken);
                break;
            case 4:
                // create online meeting
                Graph.createOnlineMeeting(accessToken);
                break;
            case 5:
                List<Team> teamList = Graph.myJoinedTeams(accessToken);
                System.out.println("My Joined Teams: "+ teamList.get(0).getRawObject());
                break;
            default:
                System.out.println("Invalid choice");
        }
    }

    // <FormatDateSnippet>
    private static String formatDateTimeTimeZone(DateTimeTimeZone date) {
        LocalDateTime dateTime = LocalDateTime.parse(date.dateTime);

        return dateTime.format(
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)) +
            " (" + date.timeZone + ")";
    }
    // </FormatDateSnippet>

    // <ListEventsSnippet>
    private static void listCalendarEvents(String accessToken) {
        // Get the user's events
        List<Event> events = Graph.getEvents(accessToken);

        System.out.println("Events:");

        for (Event event : events) {
            System.out.println("Subject: " + event.subject);
            System.out.println("  Organizer: " + event.organizer.emailAddress.name);
            System.out.println("  Start: " + formatDateTimeTimeZone(event.start));
            System.out.println("  End: " + formatDateTimeTimeZone(event.end));
        }

        System.out.println();
    }
    // </ListEventsSnippet>
}
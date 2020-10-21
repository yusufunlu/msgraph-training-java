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
        final String accessToken = "eyJ0eXAiOiJKV1QiLCJub25jZSI6InFJTkxEYTJCZzU4WHdfX0RhOGJJSjlvNUpKS216dDRUaElXeS1xZldVVkUiLCJhbGciOiJSUzI1NiIsIng1dCI6ImtnMkxZczJUMENUaklmajRydDZKSXluZW4zOCIsImtpZCI6ImtnMkxZczJUMENUaklmajRydDZKSXluZW4zOCJ9.eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC8zOWQ3NzE0Ny1iYmZlLTRlODUtOGI5Ny00MWFlNjY5OGI1MDMvIiwiaWF0IjoxNjAzMTg3NjA2LCJuYmYiOjE2MDMxODc2MDYsImV4cCI6MTYwMzE5MTUwNiwiYWNjdCI6MCwiYWNyIjoiMSIsImFpbyI6IkUyUmdZT0JLNlB0Yi9OZFkvdjRURmNZZDduVUwvNmh4THB6Qzh6UkwxUFp0MHljLzMvc0EiLCJhbXIiOlsicHdkIl0sImFwcF9kaXNwbGF5bmFtZSI6IkphdmEgR3JhcGggVHV0b3JpYWwiLCJhcHBpZCI6ImQyZGZmNTZjLTAyOWMtNDJmNC1iNjAzLWYwYzI3MmZiOGUxMSIsImFwcGlkYWNyIjoiMSIsImZhbWlseV9uYW1lIjoidW5sdSIsImdpdmVuX25hbWUiOiJ5dXN1ZiIsImlkdHlwIjoidXNlciIsImlwYWRkciI6IjIxMi4yLjIxMi4xNTYiLCJuYW1lIjoieXVzdWZ1Iiwib2lkIjoiNjFjZmQ1MmEtZTliMS00NWQ5LWFjZDQtMmQ5ZWZlNDhmMmZlIiwicGxhdGYiOiIxNCIsInB1aWQiOiIxMDAzMjAwMEVFNkEzNTI3IiwicmgiOiIwLkFBQUFSM0hYT2Y2N2hVNkxsMEd1WnBpMUEyejEzOUtjQXZSQ3RnUHd3bkw3amhGZ0FQWS4iLCJzY3AiOiJOb3Rlcy5DcmVhdGUgTm90ZXMuUmVhZCBOb3Rlcy5SZWFkLkFsbCBOb3Rlcy5SZWFkV3JpdGUgTm90ZXMuUmVhZFdyaXRlLkFsbCBOb3Rlcy5SZWFkV3JpdGUuQ3JlYXRlZEJ5QXBwIE9ubGluZU1lZXRpbmdzLlJlYWQgT25saW5lTWVldGluZ3MuUmVhZFdyaXRlIFRlYW0uQ3JlYXRlIFRlYW0uUmVhZEJhc2ljLkFsbCBUZWFtc0FwcC5SZWFkIFRlYW1zQXBwLlJlYWQuQWxsIFRlYW1zQXBwLlJlYWRXcml0ZSBUZWFtc0FwcC5SZWFkV3JpdGUuQWxsIFVzZXIuUmVhZCBwcm9maWxlIG9wZW5pZCBlbWFpbCIsInN1YiI6IjhFV29TSEhUZ29MMjU2SzJWWkN6MDE1MjkybWpSZjZ2cUMtUUphcFZRQzAiLCJ0ZW5hbnRfcmVnaW9uX3Njb3BlIjoiRVUiLCJ0aWQiOiIzOWQ3NzE0Ny1iYmZlLTRlODUtOGI5Ny00MWFlNjY5OGI1MDMiLCJ1bmlxdWVfbmFtZSI6Inl1c3VmdUBkZWFscm9vbWV2ZW50czIwMi5vbm1pY3Jvc29mdC5jb20iLCJ1cG4iOiJ5dXN1ZnVAZGVhbHJvb21ldmVudHMyMDIub25taWNyb3NvZnQuY29tIiwidXRpIjoiXzJkOFRVTHo3a3ViYm9LeFVPN1FBQSIsInZlciI6IjEuMCIsIndpZHMiOlsiYmFmMzdiM2EtNjEwZS00NWRhLTllNjItZDlkMWU1ZTg5MTRiIiwiNjJlOTAzOTQtNjlmNS00MjM3LTkxOTAtMDEyMTc3MTQ1ZTEwIiwiYjc5ZmJmNGQtM2VmOS00Njg5LTgxNDMtNzZiMTk0ZTg1NTA5Il0sInhtc19zdCI6eyJzdWIiOiIxVHNuVi01TGhVVlN4eHB0dktDT0phZUVuYWNoZExHbWJ4ZWtNMXRUTTFFIn0sInhtc190Y2R0IjoxNjAyNTExOTg4fQ.BQGq9nM2JZEGyFF-uQOGNL9TAaz8wzw4oyflp7eK8__6JV39E6BTQJkMGff1IjFLNgvH5KhISsTjq4hibXhDx-RwJVpCuyE3NoDDZZJg_1TDYakqst28D4kXsD7DsMJDGmyqwsoHQZVMERqsFaea6Cxdus9shsvq0_CFE8UOgM0zXwcTRMRZKJCIGs2w91oZBiQpRbji1jq1LT17NwlHFVVYJMtTOaeFaTaAckLSZ34cOgpKL1ys7b69tEh4nPTQlQGXfMcRpxFf0hbY2YCrl8K-Rqu4VdEOQ7S-9WCzJF9O6mfb-E_1al-XQhbaqp2ZNiso84sHbSTW-OnmxfGCiA";
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
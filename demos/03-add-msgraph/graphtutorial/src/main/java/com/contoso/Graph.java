// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.contoso;

import java.util.LinkedList;
import java.util.List;

import com.microsoft.graph.logger.DefaultLogger;
import com.microsoft.graph.logger.LoggerLevel;
import com.microsoft.graph.models.extensions.Event;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.PasswordProfile;
import com.microsoft.graph.models.extensions.User;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.microsoft.graph.requests.extensions.IEventCollectionPage;
import com.microsoft.graph.requests.extensions.IUserCollectionPage;

/**
 * Graph
 */
public class Graph {

    private static IGraphServiceClient graphClient = null;
    private static SimpleAuthProvider authProvider = null;

    private static IGraphServiceClient appOnlyGraphClient = null;
    private static SimpleAuthProvider appOnlyAuthProvider = null;

    private static void ensureGraphClient(String accessToken) {
        if (graphClient == null) {
            // Create the auth provider
            authProvider = new SimpleAuthProvider(accessToken);

            // Create default logger to only log errors
            DefaultLogger logger = new DefaultLogger();
            logger.setLoggingLevel(LoggerLevel.ERROR);

            // Build a Graph client
            graphClient = GraphServiceClient.builder()
                .authenticationProvider(authProvider)
                .logger(logger)
                .buildClient();
        }
    }

    private static void ensureAppOnlyClient(String appToken) {
        if (appOnlyGraphClient == null) {
            // Create the auth provider
            appOnlyAuthProvider = new SimpleAuthProvider(appToken);

            // Create default logger to only log errors
            DefaultLogger logger = new DefaultLogger();
            logger.setLoggingLevel(LoggerLevel.ERROR);

            // Build a Graph client
            appOnlyGraphClient = GraphServiceClient.builder()
                .authenticationProvider(appOnlyAuthProvider)
                .logger(logger)
                .buildClient();
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
            .select("subject,organizer,start,end")
            .get();

        return eventPage.getCurrentPage();
    }

    public static User createUser(String appToken, String displayName, String mailNickname, String upn) {
        ensureAppOnlyClient(appToken);

        User newUser = new User();

        newUser.displayName = displayName;
        newUser.mailNickname = mailNickname;
        newUser.userPrincipalName = upn;
        newUser.accountEnabled = true;
        newUser.passwordProfile = new PasswordProfile();
        newUser.passwordProfile.forceChangePasswordNextSignIn = true;
        newUser.passwordProfile.password = "poiqwe1!";

        return appOnlyGraphClient.users().buildRequest().post(newUser);
    }

    public static User getUserByUpn(String appToken, String upn) {
        ensureAppOnlyClient(appToken);

        return appOnlyGraphClient
            .users(upn)
            .buildRequest()
            .get();
    }

    public static void updateUser(String appToken, String userId, User userToUpdate) {
        ensureAppOnlyClient(appToken);

        appOnlyGraphClient
            .users(userId)
            .buildRequest()
            .patch(userToUpdate);
    }
}
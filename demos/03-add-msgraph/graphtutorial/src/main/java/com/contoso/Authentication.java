// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.contoso;
import java.net.MalformedURLException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.function.Consumer;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.DeviceCode;
import com.microsoft.aad.msal4j.DeviceCodeFlowParameters;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;
import com.microsoft.aad.msal4j.PublicClientApplication;

/**
 * Authentication
 */
public class Authentication {

    private static String applicationId = null;
    // Set authority to allow only organizational accounts
    // Device code flow only supports organizational accounts
    private final static String authority = "https://login.microsoftonline.com/organizations/";

    public static void initialize(String applicationId) {
        Authentication.applicationId = applicationId;
    }

    public static String getUserAccessToken(String[] scopes) {
        if (applicationId == null) {
            System.out.println("You must initialize Authentication before calling getUserAccessToken");
            return null;
        }

        Set<String> scopeSet = Set.of(scopes);

        PublicClientApplication app;
        try {
            // Build the MSAL application object with
            // app ID and authority
            app = PublicClientApplication.builder(applicationId)
                .authority(authority)
                .build();
        } catch (MalformedURLException e) {
            return null;
        }

        // Create consumer to receive the DeviceCode object
        // This method gets executed during the flow and provides
        // the URL the user logs into and the device code to enter
        Consumer<DeviceCode> deviceCodeConsumer = (DeviceCode deviceCode) -> {
            // Print the login information to the console
            System.out.println(deviceCode.message());
        };

        // Request a token, passing the requested permission scopes
        IAuthenticationResult result = app.acquireToken(
            DeviceCodeFlowParameters
                .builder(scopeSet, deviceCodeConsumer)
                .build()
        ).exceptionally(ex -> {
            System.out.println("Unable to authenticate - " + ex.getMessage());
            return null;
        }).join();

        if (result != null) {
            return result.accessToken();
        }

        return null;
    }

    public static String getAppOnlyToken(String appId, String tenantId, String secret) {
        // Simple case: using a secret
        IClientCredential credential = ClientCredentialFactory.create(secret);

        // To use a certificate instead, do something like below
        //PrivateKey privateKey;
        //X509Certificate publicKey;
        //IClientCredential certCredential = ClientCredentialFactory.create(privateKey, publicKey);

        ConfidentialClientApplication app;
        try {
            // Build the MSAL application object with
            // app ID and authority
            app = ConfidentialClientApplication.builder(appId, credential)
                .authority(String.format("https://login.microsoftonline.com/%s", tenantId))
                .build();

        } catch (MalformedURLException e) {
            return null;
        }

        Set<String> scopes = Set.of("https://graph.microsoft.com/.default");

        IAuthenticationResult result = app.acquireToken(
            ClientCredentialParameters
                .builder(scopes)
                .build()
        ).exceptionally(ex -> {
            System.out.println("Unable to authenticate - " + ex.getMessage());
            return null;
        }).join();

        if (result != null) {
            return result.accessToken();
        }

        return null;
    }
}
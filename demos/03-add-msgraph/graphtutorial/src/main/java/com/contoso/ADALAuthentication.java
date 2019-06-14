// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.contoso;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.microsoft.aad.adal4j.AsymmetricKeyCredential;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.aad.adal4j.DeviceCode;

/**
 * ADALAuthentication
 */
public class ADALAuthentication {
    private static String applicationId = null;
    // Set authority to allow only organizational accounts
    // Device code flow only supports organizational accounts
    private final static String authority = "https://login.microsoftonline.com/organizations/";

    public static void initialize(String applicationId) {
        ADALAuthentication.applicationId = applicationId;
    }

    public static String getUserAccessToken(String resource) {
        if (applicationId == null) {
            System.out.println("You must initialize Authentication before calling getUserAccessToken");
            return null;
        }

        ExecutorService service = Executors.newFixedThreadPool(1);
        AuthenticationResult result = null;

        try {
            AuthenticationContext authContext = new AuthenticationContext("https://login.microsoftonline.com/common/", true, service);

            DeviceCode deviceCode = authContext.acquireDeviceCode(applicationId, resource, null).get();

            System.out.println(deviceCode.getMessage());
            System.out.println("Press ENTER once you've completed the sign-in process in your browser.");
            System.in.read();

            result = authContext.acquireTokenByDeviceCode(deviceCode, null).get();
        } catch (MalformedURLException ex) {
            System.out.println("MalformedURLException: " + ex.getMessage());
            return null;
        } catch (InterruptedException ex) {
            System.out.println("InterruptedException: " + ex.getMessage());
            return null;
        } catch (ExecutionException ex) {
            System.out.println("ExecutionException: " + ex.getMessage());
            return null;
        } catch (IOException ex) {
            System.out.println("IOException: " + ex.getMessage());
            return null;
        } finally {
            service.shutdown();
        }

        return result == null ? null : result.getAccessToken();
    }

    public static String getAppOnlyToken(String resource, String appId, String tenantId, String secret) {
        ExecutorService service = Executors.newFixedThreadPool(1);
        AuthenticationResult result = null;

        // Simple case: using a secret
        ClientCredential credential = new ClientCredential(appId, secret);

        // To use a certificate instead, do something like below
        //PrivateKey privateKey;
        //X509Certificate publicKey;
        //AsymmetricKeyCredential certCredential = AsymmetricKeyCredential.create(appId, privateKey, publicKey);

        try {
            AuthenticationContext authContext = new AuthenticationContext(
                String.format("https://login.microsoftonline.com/%s", tenantId), true, service);

            result = authContext.acquireToken(resource, credential, null).get();
        } catch (MalformedURLException ex) {
            System.out.println("MalformedURLException: " + ex.getMessage());
            return null;
        } catch (InterruptedException ex) {
            System.out.println("InterruptedException: " + ex.getMessage());
            return null;
        } catch (ExecutionException ex) {
            System.out.println("ExecutionException: " + ex.getMessage());
            return null;
        } finally {
            service.shutdown();
        }

        return result == null ? null : result.getAccessToken();
    }
}
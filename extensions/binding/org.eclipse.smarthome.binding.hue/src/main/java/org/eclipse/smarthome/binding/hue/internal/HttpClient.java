/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.hue.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.HttpMethod;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
@NonNullByDefault
public class HttpClient {
    private int timeout = 1000;
    private final Logger logger = LoggerFactory.getLogger(HttpClient.class);
    private final LinkedList<AsyncPutParameters> commandsQueue = new LinkedList<>();
    private @Nullable Future<?> job;
    private PublicKey pub;
    private final String ip;

    public HttpClient(String ip) {
        this.ip = ip;
    }

    @SuppressWarnings({ "null", "unused" })
    private void executeCommands() {
        while (true) {
            try {
                long delayTime = 0;
                synchronized (commandsQueue) {
                    AsyncPutParameters payloadCallbackPair = commandsQueue.poll();
                    if (payloadCallbackPair != null) {
                        logger.debug("Async sending put to address: {} delay: {} body: {}", payloadCallbackPair.address,
                                payloadCallbackPair.delay, payloadCallbackPair.body);
                        try {
                            Result result = put(payloadCallbackPair.address, payloadCallbackPair.body);
                            payloadCallbackPair.future.complete(result);
                        } catch (IOException e) {
                            payloadCallbackPair.future.completeExceptionally(e);
                        }
                        delayTime = payloadCallbackPair.delay;
                    } else {
                        return;
                    }
                }
                Thread.sleep(delayTime);
            } catch (InterruptedException e) {
                logger.debug("commandExecutorThread was interrupted", e);
            }
        }
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Result get(String address) throws IOException {
        return doNetwork(address, HttpMethod.GET);
    }

    public Result post(String address, String body) throws IOException {
        return doNetwork(address, HttpMethod.POST, body);
    }

    public Result put(String address, String body) throws IOException {
        return doNetwork(address, HttpMethod.PUT, body);
    }

    public CompletableFuture<Result> putAsync(String address, String body, long delay,
            ScheduledExecutorService scheduler) {
        AsyncPutParameters asyncPutParameters = new AsyncPutParameters(address, body, delay);

        synchronized (commandsQueue) {
            if (commandsQueue.isEmpty()) {
                commandsQueue.offer(asyncPutParameters);
                if (job == null || job.isDone()) {
                    job = scheduler.submit(this::executeCommands);
                }
            } else {
                commandsQueue.offer(asyncPutParameters);
            }
        }

        return asyncPutParameters.future;
    }

    public Result delete(String address) throws IOException {
        return doNetwork(address, HttpMethod.DELETE);
    }

    protected Result doNetwork(String address, String requestMethod) throws IOException {
        return doNetwork(address, requestMethod, null);
    }

    protected Result doNetwork(String address, String requestMethod, @Nullable String body) throws IOException {
        HttpURLConnection conn = null;
        try {
            // first try to use https
            conn = enrichConnection(doHttps(address), requestMethod);
            return getResult(conn, body);
        } catch(Exception httpsEx) {
            logger.debug("hue bridge not reachable over https");
            try {
                // then try to use http
                conn = enrichConnection(doHttp(address), requestMethod);
                return getResult(conn, body);
            } catch(Exception httpEx) {
                logger.debug("hue bridge not reachable over http");
            }
        } finally {
            try {
                // clean up connection
                conn.disconnect();
            } catch(NullPointerException e) {
                // no connection to close
            }
        }

        return null;
    }

    /**
     * Configure https connection
     * 
     * @param address relative url
     * @return HttpsURLConnection
     * @throws MalformedURLException
     * @throws IOException
     */
    private HttpsURLConnection doHttps(String address) throws MalformedURLException, IOException {
        HttpsURLConnection conn = (HttpsURLConnection) new URL("https://" + ip + ":443" + address).openConnection();

        // trusting anything
        TrustManager[] permitAll = new TrustManager[] {
            new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };

        // verifying anything
        HostnameVerifier hsv = new HostnameVerifier() {
            @Override
            public boolean verify(String ip, SSLSession ssls) {
                try {
                    X509Certificate x509c = (X509Certificate) ssls.getPeerCertificates()[0];
                    PublicKey newPub = x509c.getPublicKey();
                    if (pub != newPub) {
                        // public key changed
                        logger.warn("hue bridge certificate public key has changed");
                    }
                } catch(SSLPeerUnverifiedException e) {
                    logger.warn("hue bridge certificate isn't valid");
                }
                // we can't use the default verification because the certificates identification
                // hostname (hue mac address) isn't matching the specified host (hue ip address)
                return true;
            }
        };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, permitAll, new SecureRandom());
            conn.setSSLSocketFactory(sc.getSocketFactory());
        } catch(Exception e) {
            logger.error("Failed configuring ssl socket connection!");
            return null;
        }
        conn.setHostnameVerifier(hsv);
        return conn;
    }

    /**
     * Configure http connection
     * 
     * @param address relative url
     * @return HttpURLConnection
     * @throws MalformedURLException
     * @throws IOException
     */
    private HttpURLConnection doHttp(String address) throws MalformedURLException, IOException {
        return (HttpURLConnection) new URL("http://" + ip + address).openConnection();
    }

    /**
     * Set connection params
     * 
     * @param conn connection
     * @param requestMethod http request method
     * @return HttpURLConnection
     */
    private HttpURLConnection enrichConnection(HttpURLConnection conn, String requestMethod) {
        try {
            conn.setRequestMethod(requestMethod);
        } catch(ProtocolException e) {
            logger.error("Use 'HttpMethod' to specify method!");
        }
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(timeout);
        conn.setReadTimeout(timeout);
        return conn;
    }

    /**
     * Send request and return result
     * 
     * @param conn connection
     * @param body text for the request body
     * @return Result
     * @throws IOException
     */
    private Result getResult(HttpURLConnection conn, @Nullable String body) throws IOException {
        if (body != null && !"".equals(body)) {
            conn.setDoOutput(true);
            try (Writer out = new OutputStreamWriter(conn.getOutputStream())) {
                out.write(body);
            }
        }
        try (InputStream in = conn.getInputStream(); ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return new Result(result.toString(StandardCharsets.UTF_8.name()), conn.getResponseCode());
        }
    }

    public static class Result {
        private final String body;
        private final int responseCode;

        public Result(String body, int responseCode) {
            this.body = body;
            this.responseCode = responseCode;
        }

        public String getBody() {
            return body;
        }

        public int getResponseCode() {
            return responseCode;
        }
    }

    public final class AsyncPutParameters {
        public final String address;
        public final String body;
        public final CompletableFuture<Result> future;
        public final long delay;

        public AsyncPutParameters(String address, String body, long delay) {
            this.address = address;
            this.body = body;
            this.future = new CompletableFuture<>();
            this.delay = delay;
        }
    }
}

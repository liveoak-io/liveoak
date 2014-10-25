/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.analytics;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class Analytics {

    private static final Logger log = Logger.getLogger(Analytics.class);
    private static final Logger consoleLog = Logger.getLogger(Analytics.class.getName() + ".log");

    private final int MAX = 1000;

    private ConcurrentLinkedQueue<AnalyticsEvent> events = new ConcurrentLinkedQueue<>();

    private AnalyticsExtensionConfig config;
    private AnalyticsProcessor thread;
    private ObjectMapper mapper;

    public Analytics() {
        JsonFactory factory = new JsonFactory();
        mapper = new ObjectMapper(factory);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public void event(AnalyticsEvent event) {
        events.add(event);
        consoleLog.info(event);
    }

    public void start() {
        // start event processing thread
        thread = new AnalyticsProcessor();
        thread.start();
    }

    public void stop() {
        // stop event processing thread
        thread.finish();
    }

    public void config(AnalyticsExtensionConfig config) {
        this.config = config;
    }

    private class AnalyticsProcessor extends Thread {

        volatile boolean finish = false;

        public void run() {
            while (!finish) {

                LinkedList<AnalyticsEvent> batch = new LinkedList<>();
                AnalyticsEvent event = null;
                for (int count = 0; config != null && count < MAX && (event = events.poll()) != null; count ++) {
                    batch.add(event);
                }

                if (batch.size() > 0) {
                    sendToService(batch);
                }

                try {
                    if (!finish) {
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException("Interrupted!");
                }
            }
        }

        private void sendToService(LinkedList<AnalyticsEvent> batch) {

            try {
                URL url = new URL(config.rhqMetricsUrl());
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);

                ByteArrayOutputStream buffed = new ByteArrayOutputStream();
                OutputStream os = new BufferedOutputStream(con.getOutputStream());
                try {
                    ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
                    writer.writeValue(buffed, batch);

                    os.write(buffed.toByteArray());
                } finally {
                    os.close();
                }

                int status = con.getResponseCode();
                if (status != 200) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    InputStream result = con.getErrorStream();
                    if (result != null) {
                        try {
                            copy(result, baos);
                        } catch (Exception ignored) {
                            log.warn("[IGNORED] Failed to read HTTP response from rhq-metrics: ", ignored);
                        }
                    }

                    log.error("Failed to post " + batch.size() + " analytics events to rhq-metrics (" + url + "): " + status + " " + con.getResponseMessage()
                            + "\n" + new String(baos.toByteArray(), "utf-8"));
                }

            } catch (Exception e) {
                // determine if error might be recoverable
                // if not, log the error, and return
                log.error("Failure during analytics event queue processing: ", e);
            }
        }

        private void copy(InputStream is, OutputStream os) throws IOException {
            byte [] buff = new byte[8*1024];
            int rc;
            try {
                while ((rc = is.read(buff)) != -1) {
                    os.write(buff, 0, rc);
                }
            } finally {
                try {
                    is.close();
                } catch(Exception ignored) {
                }
                os.close();
            }
        }

        void finish() {
            finish = true;
        }
    }
}

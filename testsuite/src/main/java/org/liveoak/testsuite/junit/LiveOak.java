package org.liveoak.testsuite.junit;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.liveoak.testsuite.annotations.LiveOakConfig;
import org.liveoak.testsuite.internal.BasicInjector;
import org.liveoak.testsuite.internal.LazyResource;
import org.liveoak.testsuite.internal.LiveOakServer;
import org.liveoak.testsuite.internal.WebDriverFactory;
import org.liveoak.testsuite.internal.Config;
import org.liveoak.testsuite.utils.JsExecutor;
import org.openqa.selenium.WebDriver;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class LiveOak extends BlockJUnit4ClassRunner {

    private boolean inititalized;
    private static LiveOakServer server;

    private Class<?> testClass;
    private Object test;
    private BasicInjector injector;

    public LiveOak(Class<?> testClass) throws InitializationError {
        super(testClass);
        this.testClass = testClass;
    }

    @Override
    protected Object createTest() throws Exception {
        this.test = super.createTest();
        injector.init(test);
        return test;
    }

    @Override
    public void run(RunNotifier notifier) {
        if (!inititalized) {
            notifier.addListener(new RunListener() {
                @Override
                public void testRunFinished(Result result) throws Exception {
                    System.out.println("testRunFinished");

                    for (Object o : injector.getResources().values()) {
                        try {
                            if (o instanceof Closeable) {
                                ((Closeable) o).close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (server != null) {
                        server.stop();
                        server = null;
                    }
                }
            });

            try {
                if (server == null) {
                    LiveOakConfig liveOakConfig = testClass.getAnnotation(LiveOakConfig.class);
                    if (liveOakConfig != null) {
                        server = new LiveOakServer(liveOakConfig);
                    } else {
                        server = new LiveOakServer();
                    }

                    server.start();
                }

                injector = new BasicInjector();
                injector.addResource(URL.class, new URL(Config.baseUrl()));

                WebDriverResource webDriverResource = new WebDriverResource();
                injector.addResource(WebDriver.class, webDriverResource);
                injector.addResource(JsExecutor.class, new JsExecutor(webDriverResource));

                injector.addResource(HttpClient.class, new HttpClientResource());

                inititalized = true;
            } catch (Throwable e) {
                notifier.fireTestFailure(new Failure(getDescription(), e));
                return;
            }
        }

        super.run(notifier);
    }

    public class HttpClientResource implements LazyResource<HttpClient> {

        private CloseableHttpClient client;

        @Override
        public HttpClient get() {
            if (client == null) {
                RequestConfig config = RequestConfig.custom().setConnectTimeout(1000).setConnectionRequestTimeout(1000).setSocketTimeout(1000).build();
                client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
            }
            return client;
        }

        @Override
        public void close() throws IOException {
            if (client != null) {
                client.close();
            }
        }

    }

    public class WebDriverResource implements LazyResource<WebDriver> {

        private WebDriver driver;

        @Override
        public WebDriver get() {
            if (driver == null) {
                driver = WebDriverFactory.create();
            }
            return driver;
        }

        @Override
        public void close() throws IOException {
            if (driver != null) {
                WebDriverFactory.close(driver);
            }
        }
    }

}

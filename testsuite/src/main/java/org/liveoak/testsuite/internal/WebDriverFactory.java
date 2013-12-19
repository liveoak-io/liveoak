/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.liveoak.testsuite.internal;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.rules.ExternalResource;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.support.PageFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class WebDriverFactory {

    public static WebDriver create() {
        String browser = System.getProperty("browser", "phantomjs");
        WebDriver driver;

        switch (browser) {
            case "phantomjs":
                driver = new PhantomJSDriver();
                break;
            case "chrome":
                driver = new ChromeDriver();
                break;
            case "firefox":
                driver = new FirefoxDriver();
                break;
            default:
                throw new RuntimeException("Unsupported browser " + browser + " (supported are phantomjs, chrome or firefox)");
        }

        driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);
        return driver;
    }

//    public Object execute(String script, Object... args) throws IOException, JSONException {
//        InputStream is = System.class.getResourceAsStream(script);
//        script = IOUtils.toString(is);
//        JavascriptExecutor executor = (JavascriptExecutor) driver;
//        Object result = executor.executeAsyncScript(script, args);
//        if (result == null) {
//            return null;
//        }
//
//        if (result instanceof String) {
//            String s = (String) result;
//            s = s.trim();
//            if (s.startsWith("[")) {
//                return new JSONArray(s);
//            } else if (s.startsWith("{")) {
//                return new JSONObject(s);
//            } else {
//                return s;
//            }
//        }
//
//        return result;
//    }

    public static void close(WebDriver driver) {
        driver.manage().deleteAllCookies();
        driver.quit();
    }

}

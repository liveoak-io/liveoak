/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.console.testsuite;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.jboss.arquillian.graphene.Graphene.guardHttp;
import static org.jboss.arquillian.graphene.Graphene.waitGui;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:amendonc@redhat.com">Alexandre Mendonca</a>
 */
@RunWith(Arquillian.class)
public class LoginPageIT {

    @Drone
    WebDriver browser;

    private static final String BASE_URL = "http://localhost:8080/admin/";

    @FindBy
    private WebElement username;

    @FindBy
    private WebElement password;

    @FindBy(id = "kc-login")
    private WebElement loginButton;

    @FindBy(id = "kc-feedback")
    private WebElement loginFeedback;

    @FindBy(className = "lo-user")
    private WebElement loggedUser;

    @FindBy
    private WebElement logout;

    @Test
    @InSequence(1)
    public void loginComponentsExist() {
        browser.navigate().to(BASE_URL);

        assertTrue("Check that page contains username input element", username.isDisplayed());
        assertTrue("Check that page contains password input element", password.isDisplayed());

        assertTrue("Check that page contains login button element", loginButton.isDisplayed());
    }

    @Test
    @InSequence(2)
    public void loginFails() {
        username.sendKeys("hacker");
        password.sendKeys("letmein");

        guardHttp(loginButton).click();

        assertTrue("Check that page contains login feedback element", loginFeedback.isDisplayed());
        assertEquals("Check that failed login text is correct",
                "Invalid username or password.", loginFeedback.getText().trim());
        assertTrue("Check that failed login feedback has 'feedback-error' class",
                loginFeedback.getAttribute("class").contains("feedback-error"));
    }

    @Test
    @InSequence(3)
    public void loginSucceeds() {
        username.clear();
        username.sendKeys("admin");
        password.clear();
        password.sendKeys("admin");

        guardHttp(loginButton).click();

/*
        assertEquals("Check that page title is the LiveOak Admin Console",
                "LiveOak -", browser.getTitle());
*/
        assertTrue("Check that page contains logged user element", loggedUser.isDisplayed());
        assertEquals("Check that logged user is 'admin'", "admin", loggedUser.getText().trim());

    }

    @Test
    @InSequence(4)
    public void loginPersist() {
        browser.navigate().to(BASE_URL);

        waitGui().until().element(loggedUser).is().visible();

/*
        assertEquals("Check that page title is the LiveOak Admin Console",
                "LiveOak -", browser.getTitle());
*/
        assertTrue("Check that page contains logged user element", loggedUser.isDisplayed());
        assertEquals("Check that logged user is 'admin'", "admin", loggedUser.getText().trim());
    }

    @Test
    @InSequence(5)
    public void logoutSucceeds() {
        loggedUser.click();

        guardHttp(logout).click();

        assertTrue("Check that page contains username input element", username.isDisplayed());
        assertTrue("Check that page contains password input element", password.isDisplayed());

        assertTrue("Check that page contains login button element", loginButton.isDisplayed());
    }

    @Test
    @InSequence(6)
    public void logoutPersists() {
        browser.navigate().to(BASE_URL);

        assertTrue("Check that page contains username input element", username.isDisplayed());
        assertTrue("Check that page contains password input element", password.isDisplayed());

        assertTrue("Check that page contains login button element", loginButton.isDisplayed());
    }

}
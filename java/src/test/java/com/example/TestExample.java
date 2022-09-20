package com.example;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.io.IOException;

public class TestExample {
  // Shared between all tests in this class.
  static WebView2Process webview2Process;
  static Playwright playwright;
  static Browser browser;
  static BrowserContext context;
  static Page page;

  @BeforeAll
  static void launchBrowser() throws IOException {
    playwright = Playwright.create();
    int cdpPort = 9222;
    webview2Process = new WebView2Process(cdpPort);
    browser = playwright.chromium().connectOverCDP("http://127.0.0.1:" + cdpPort);
    context = browser.contexts().get(0);
    page = context.pages().get(0);
  }

  @AfterAll
  static void closeBrowser() {
    webview2Process.dispose();
  }

  @Test
  public void shouldClickButton() {
    page.navigate("https://playwright.dev");
    Locator gettingStarted = page.locator("text=Get started");
    assertThat(gettingStarted).isVisible();
    page.pause();
  }
}
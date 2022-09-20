from playwright.sync_api import Page, expect


def test_webview2(page: Page):
    page.goto("https://playwright.dev")
    get_started = page.locator("text=Get Started")
    expect(get_started).to_be_visible()

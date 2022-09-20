import { test, expect } from './webView2Test';

test('test WebView2', async ({ page }) => {
  await page.goto('https://playwright.dev');
  const getStarted = page.locator('text=Get Started');
  expect(getStarted).toBeVisible();
});

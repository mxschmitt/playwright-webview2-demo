import { test as base } from '@playwright/test';
import fs from 'fs';
import os from 'os';
import path from 'path';
import childProcess from 'child_process';

export const test = base.extend({
  browser: async ({ playwright }, use, testInfo) => {
    const cdpPort = 10000 + testInfo.workerIndex;
    const executable = path.join(__dirname, '../../webview2-app/bin/Debug/net6.0-windows/webview2.exe');
    fs.accessSync(executable, fs.constants.X_OK); // Make sure that the executable exists and is executable
    const userDataDir = path.join(fs.realpathSync.native(os.tmpdir()), `playwright-webview2-tests/user-data-dir-${testInfo.workerIndex}`);
    const webView2Process = childProcess.spawn(executable, [], {
      shell: true,
      env: {
        ...process.env,
        WEBVIEW2_ADDITIONAL_BROWSER_ARGUMENTS: `--remote-debugging-port=${cdpPort}`,
        WEBVIEW2_USER_DATA_FOLDER: userDataDir,
      }
    });
    await new Promise<void>(resolve => webView2Process.stdout.on('data', data => {
      if (data.toString().includes('WebView2 initialized'))
        resolve();
    }));
    const browser = await playwright.chromium.connectOverCDP(`http://127.0.0.1:${cdpPort}`);
    await use(browser);
    await browser.close()
    childProcess.execSync(`taskkill /pid ${webView2Process.pid} /T /F`);
    fs.rmdirSync(userDataDir, { recursive: true });
  },
  context: async ({ browser }, use) => {
    const context = browser.contexts()[0];
    await use(context);
  },
  page: async ({ context }, use) => {
    const page = context.pages()[0];
    await use(page);
  },
});

export { expect } from '@playwright/test';

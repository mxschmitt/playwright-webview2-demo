import os
import tempfile
import pytest
from pathlib import Path
from playwright.sync_api import Playwright, Browser, BrowserContext
import subprocess


@pytest.fixture(scope="session")
def data_dir():
    with tempfile.TemporaryDirectory(
        prefix="playwright-webview2-tests", ignore_cleanup_errors=True
    ) as tmpdirname:
        yield tmpdirname


@pytest.fixture(scope="session")
def webview2_process_cdp_port(data_dir: str):
    cdp_port = 9222
    process = subprocess.Popen(
        [
            Path(__file__).parent
            / ".."
            / "webview2-app"
            / "bin"
            / "Debug"
            / "net6.0-windows"
            / "webview2.exe",
        ],
        env={
            **dict(os.environ),
            "WEBVIEW2_ADDITIONAL_BROWSER_ARGUMENTS": f"--remote-debugging-port={cdp_port}",
            "WEBVIEW2_USER_DATA_FOLDER": data_dir,
        },
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        universal_newlines=True,
    )
    while True:
        line = process.stdout.readline()
        if "WebView2 initialized" in line:
            break
    yield cdp_port
    process.terminate()


@pytest.fixture(scope="session")
def browser(playwright: Playwright, webview2_process_cdp_port: int):
    browser = playwright.chromium.connect_over_cdp(
        f"http://127.0.0.1:{webview2_process_cdp_port}"
    )
    yield browser


@pytest.fixture(scope="function")
def context(browser: Browser):
    context = browser.contexts[0]
    yield context


@pytest.fixture(scope="function")
def page(context: BrowserContext):
    page = context.pages[0]
    yield page

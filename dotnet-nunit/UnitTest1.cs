using System.Text.RegularExpressions;
using Microsoft.Playwright.NUnit;
using Microsoft.Playwright;
using System.Diagnostics;

namespace dotnet_nunit;

[Parallelizable(ParallelScope.Self)]
public class Tests : WebView2Test
{
    [Test]
    public async Task HomepageHasPlaywrightInTitleAndGetStartedLinkLinkingtoTheIntroPage()
    {
        await Page.GotoAsync("https://playwright.dev");

        var getStarted = Page.Locator("text=Get Started");
        await Expect(getStarted).ToBeVisibleAsync();
    }
}

public class WebView2Test : PlaywrightTest
{
    public IBrowser Browser { get; internal set; } = null!;
    public IBrowserContext Context { get; internal set; } = null!;
    public IPage Page { get; internal set; } = null!;
    private Process? _webView2Process = null;
    private string _userDataDir = null!;

    [SetUp]
    public async Task BrowserSetUp()
    {
        var cdpPort = 10000 + WorkerIndex;
        var executable = Path.Join(Directory.GetCurrentDirectory(), @"..\..\..\..\webview2-app\bin\Debug\net6.0-windows\webview2.exe");
        Assert.IsTrue(File.Exists(executable), "Make sure that the executable exists");
        _userDataDir = Path.Join(Path.GetTempPath(), $"playwright-webview2-tests/user-data-dir-{TestContext.CurrentContext.WorkerId}");
        // WebView2 does some lazy cleanups on shutdown so we can't clean it up after each test
        if (Directory.Exists(_userDataDir))
        {
            Directory.Delete(_userDataDir, true);
        }
        _webView2Process = Process.Start(new ProcessStartInfo(executable)
        {
            EnvironmentVariables =
        {
            ["WEBVIEW2_ADDITIONAL_BROWSER_ARGUMENTS"] = $"--remote-debugging-port={cdpPort}",
            ["WEBVIEW2_USER_DATA_FOLDER"] = _userDataDir,
        },
            RedirectStandardOutput = true,
        });
        while (true)
        {
            var output = await _webView2Process!.StandardOutput.ReadLineAsync();
            if (output != null && output.Contains("WebView2 initialized"))
            {
                break;
            }
        }
        Browser = await Playwright.Chromium.ConnectOverCDPAsync($"http://127.0.0.1:{cdpPort}");
        Context = Browser.Contexts[0];
        Page = Context.Pages[0];
    }

    [TearDown]
    public async Task BrowserTearDown()
    {
        _webView2Process!.Kill(true);
        await Browser.CloseAsync();
    }
}
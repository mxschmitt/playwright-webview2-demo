using Microsoft.Playwright.NUnit;

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

package bg.smoc;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;

public class GradingResultsAfterEndOfContestTest {

    private WebClient webClient;
    private HtmlPage page;

    @Before
    public void setUp() {
        webClient = new WebClient(BrowserVersion.FIREFOX_2);
    }

    @Test
    public void printPageOutput() throws FailingHttpStatusCodeException, MalformedURLException,
            IOException {
        page = (HtmlPage) webClient.getPage(TestConstants.BASE_URL + "judge");
        HtmlForm form = page.getFormByName("PasswordForm");
        assertNotNull(form);
        ((HtmlPasswordInput) form.getInputByName("pw")).setValueAttribute("L1m0Sm0c");
        page = (HtmlPage) form.submit(form.getInputByName("connectButton"));
        assertNotNull(page);
        page = (HtmlPage) page.getAnchorByHref("contestSetup").click();
        assertNotNull(page);

        // TODO(zbogi): The following code needs to be run a single time in
        // order for this test to pass. Eventually we should drop it and have
        // the data store pre-populated with data.

        // HtmlForm addContestForm = page.getFormByName("AddNewContestForm");
        // ((HtmlTextInput)
        // addContestForm.getInputByName("name")).setValueAttribute("Test
        // contest 1");
        // page = (HtmlPage) ((HtmlSubmitInput)
        // addContestForm.getInputByValue("Add contest")).click();
        // assertNotNull(page);

        // TODO(zbogi): Anchors shouldn't be accessed by href. Accessing them by
        // name will make our tests less fragile.
        page = (HtmlPage) page.getAnchorByHref("editContest?id=Contest_0").click();

        // TODO(zbogi): The following two rows should not be needed if we had
        // setup the test data correctly.
        try {
            page = (HtmlPage) page.getAnchorByName("disableAnalysisMode").click();
            page = (HtmlPage) page.getAnchorByHref("editContest?id=Contest_0").click();
        } catch (ElementNotFoundException ex) {
            // We actually expect this. However there is no simple way to check
            // it.
        }

        page = (HtmlPage) page.getAnchorByName("enableAnalysisMode").click();
        page = (HtmlPage) page.getAnchorByHref("editContest?id=Contest_0").click();
        try {
            page.getAnchorByName("enableAnalysisMode");
            fail();
        } catch (ElementNotFoundException ex) {
        }

        page = (HtmlPage) page.getAnchorByName("disableAnalysisMode").click();
        page = (HtmlPage) page.getAnchorByHref("editContest?id=Contest_0").click();

        try {
            page.getAnchorByName("disableAnalysisMode");
            fail();
        } catch (ElementNotFoundException e) {
        }
        page.getAnchorByName("enableAnalysisMode");

        System.out.println(page.getForms());
        System.out.println(page.asText());
    }
}

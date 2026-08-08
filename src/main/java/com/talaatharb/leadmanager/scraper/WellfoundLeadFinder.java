package com.talaatharb.leadmanager.scraper;

import com.talaatharb.leadmanager.entity.SalesLead;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Discovers sales leads from <a href="https://wellfound.com/jobs">Wellfound</a>
 * (formerly AngelList Talent).
 *
 * <p>Wellfound lists startup job postings, making it an excellent source of
 * early-stage company leads actively looking for hires / partnerships.
 *
 * <h3>Guest mode (stub)</h3>
 * Wellfound's job search page is a fully client-side React Single-Page App.
 * Static jsoup requests receive only a minimal HTML shell without job data.
 * A guest scrape is therefore not feasible without a headless browser.
 * The guest path logs a clear warning and returns an empty list.
 *
 * <h3>Authenticated mode (stub)</h3>
 * Wellfound provides a private API consumed by its own frontend.  With a valid
 * session cookie ({@code user_session_token} or {@code angellist_session}) the
 * private endpoints could be called directly.  This path is also stubbed until
 * headless-browser support or the API contract is confirmed.
 *
 * <p><b>TODO – full implementation options:</b>
 * <ol>
 *   <li>Integrate a headless browser (e.g. Playwright / Selenium) and navigate
 *       to {@code https://wellfound.com/jobs} after logging in.  Wait for the
 *       React hydration, then extract {@code data-test} attributes or
 *       {@code aria-label} values.</li>
 *   <li>Reverse-engineer the GraphQL / REST endpoints the Wellfound SPA calls
 *       and replicate those requests with the session cookie attached.</li>
 * </ol>
 */
public class WellfoundLeadFinder implements LeadFinder {

    private static final Logger log = LoggerFactory.getLogger(WellfoundLeadFinder.class);

    private static final String WELLFOUND_JOBS_URL = "https://wellfound.com/jobs";

    @Override
    public String getName() {
        return "Wellfound (AngelList)";
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Current status: stub.</b>  Wellfound's job listings are rendered
     * entirely by a React SPA.  Static HTTP requests return only a page shell
     * without job data.  This method logs a warning and returns an empty list
     * until a headless-browser integration is implemented.
     */
    @Override
    public List<SalesLead> findLatestHotLeads(ScraperConfig config) {
        if (config.isAuthenticated()) {
            return scrapeAuthenticated(config);
        } else {
            return scrapeGuest(config);
        }
    }

    // ---- Private helpers ----

    /**
     * Guest scrape attempt.
     * <p>
     * Returns an empty list and logs a warning because Wellfound does not
     * serve job listings in static HTML.
     */
    private List<SalesLead> scrapeGuest(ScraperConfig config) {
        log.warn("WellfoundLeadFinder (guest): Wellfound is a fully client-side React app. "
                + "Static HTML scraping is not supported. "
                + "Supply session cookies via ScraperConfig or implement headless-browser support.");

        // Attempt a best-effort static fetch so we have something to log
        try {
            Connection conn = Jsoup.connect(WELLFOUND_JOBS_URL)
                    .userAgent(config.getUserAgent())
                    .timeout(config.getTimeoutMs())
                    .followRedirects(true);
            Document doc = conn.get();
            log.debug("WellfoundLeadFinder (guest): fetched {} chars of HTML, "
                    + "but no job cards expected in static response.", doc.html().length());
        } catch (IOException e) {
            log.error("WellfoundLeadFinder (guest) HTTP request failed", e);
        }

        return new ArrayList<>();
    }

    /**
     * Authenticated scrape attempt.
     * <p>
     * Attaches session cookies and performs a static fetch.  Because Wellfound
     * uses client-side rendering the response will still lack job data.
     *
     * <p><b>TODO:</b> Replace this stub with either:
     * <ul>
     *   <li>A headless-browser flow that navigates to the jobs page after login, or</li>
     *   <li>Direct calls to the internal Wellfound API endpoints discovered via
     *       browser DevTools (Network tab) after authenticating.</li>
     * </ul>
     */
    private List<SalesLead> scrapeAuthenticated(ScraperConfig config) {
        log.warn("WellfoundLeadFinder (authenticated): session cookies detected, "
                + "but full scraping requires headless-browser support. "
                + "This is a stub — returning empty list.");

        try {
            Connection conn = Jsoup.connect(WELLFOUND_JOBS_URL)
                    .userAgent(config.getUserAgent())
                    .timeout(config.getTimeoutMs())
                    .followRedirects(true);
            config.getCookies().forEach(conn::cookie);

            Document doc = conn.get();

            // TODO: once headless rendering is available, parse real job cards here.
            // Example selector to try once SPA is rendered:
            //   Elements cards = doc.select("div[data-test=StartupResult]");
            Elements cards = doc.select("div[data-test=StartupResult]");

            List<SalesLead> leads = new ArrayList<>();
            for (Element card : cards) {
                String company = card.selectFirst("a[data-test=startup-link]") != null
                        ? card.selectFirst("a[data-test=startup-link]").text().trim()
                        : "";
                String website = card.selectFirst("a[data-test=startup-link]") != null
                        ? "https://wellfound.com" + card.selectFirst("a[data-test=startup-link]").attr("href")
                        : "";

                if (company.isBlank()) {
                    continue;
                }

                SalesLead lead = new SalesLead();
                lead.setCompany(company);
                lead.setName(company);
                lead.setWebsite(website);
                lead.setSource(getName());
                leads.add(lead);
            }

            if (!leads.isEmpty()) {
                log.info("WellfoundLeadFinder (authenticated) found {} leads", leads.size());
            }
            return leads;
        } catch (IOException e) {
            log.error("WellfoundLeadFinder (authenticated) HTTP request failed", e);
            return new ArrayList<>();
        }
    }
}

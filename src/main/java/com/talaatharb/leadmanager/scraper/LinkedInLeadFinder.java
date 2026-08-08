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
 * Discovers sales leads from <a href="https://www.linkedin.com/jobs/search/">LinkedIn Jobs</a>.
 *
 * <h3>Guest mode</h3>
 * LinkedIn heavily guards its HTML behind JavaScript rendering and bot-detection.
 * The guest scraping path requests the publicly accessible job search endpoint and
 * parses whatever static HTML LinkedIn serves.  Results may be limited or empty
 * depending on LinkedIn's current bot-mitigation posture.
 *
 * <h3>Authenticated mode</h3>
 * Supply a valid {@code li_at} session cookie via {@link ScraperConfig#cookie(String, String)}.
 * When present the scraper attaches it to every request, which gives access to the
 * full authenticated job-search results page.
 *
 * <pre>{@code
 * ScraperConfig cfg = ScraperConfig.builder()
 *         .cookie("li_at", "<your-linkedin-session-cookie>")
 *         .build();
 * List<SalesLead> leads = new LinkedInLeadFinder().findLatestHotLeads(cfg);
 * }</pre>
 *
 * <p><b>Note:</b> Scraping LinkedIn may violate their Terms of Service.
 * Use this scraper only with accounts and data you are authorised to access.
 */
public class LinkedInLeadFinder implements LeadFinder {

    private static final Logger log = LoggerFactory.getLogger(LinkedInLeadFinder.class);

    /**
     * Public job search endpoint.  Authenticated requests can use
     * {@code https://www.linkedin.com/jobs/search/?keywords=...} with the
     * {@code li_at} cookie to get richer results.
     */
    private static final String LINKEDIN_JOBS_URL =
            "https://www.linkedin.com/jobs/search/?keywords=software&location=Worldwide&f_WT=2";

    @Override
    public String getName() {
        return "LinkedIn Jobs";
    }

    @Override
    public List<SalesLead> findLatestHotLeads(ScraperConfig config) {
        List<SalesLead> leads = new ArrayList<>();

        if (config.isAuthenticated()) {
            leads.addAll(scrapeAuthenticated(config));
        } else {
            leads.addAll(scrapeGuest(config));
        }

        log.info("LinkedInLeadFinder found {} leads (authenticated={})",
                leads.size(), config.isAuthenticated());
        return leads;
    }

    // ---- Private helpers ----

    /**
     * Guest scrape: fetch the public jobs search page and parse job cards.
     * LinkedIn may return an anti-bot challenge page; in that case an empty
     * list is returned and a warning is logged.
     */
    private List<SalesLead> scrapeGuest(ScraperConfig config) {
        List<SalesLead> leads = new ArrayList<>();
        try {
            Document doc = buildConnection(LINKEDIN_JOBS_URL, config).get();

            // LinkedIn public job cards (as of 2024-Q2)
            Elements cards = doc.select("div.base-card");
            if (cards.isEmpty()) {
                log.warn("LinkedInLeadFinder (guest): no job cards found – "
                        + "LinkedIn may be serving a bot-challenge page. "
                        + "Consider supplying an authenticated session cookie.");
                return leads;
            }

            for (Element card : cards) {
                String title   = text(card, "h3.base-search-card__title");
                String company = text(card, "h4.base-search-card__subtitle");
                String location = text(card, "span.job-search-card__location");
                String url = attr(card, "a.base-card__full-link", "href");

                SalesLead lead = new SalesLead();
                lead.setName(title.isBlank() ? company : title);
                lead.setCompany(company);
                lead.setNotes("Location: " + location);
                lead.setWebsite(url);
                lead.setSource(getName());
                leads.add(lead);
            }
        } catch (IOException e) {
            log.error("LinkedInLeadFinder (guest) failed", e);
        }
        return leads;
    }

    /**
     * Authenticated scrape: attach the {@code li_at} session cookie (and any
     * other cookies from the config) so LinkedIn serves the full job search page.
     *
     * <p>TODO: LinkedIn's authenticated job-search page is largely rendered by
     * React/Ember and requires a headless browser (e.g. Playwright) to obtain
     * fully hydrated HTML.  This stub sends the request with the session cookie;
     * replace the selector logic below once a headless-browser integration is added.
     */
    private List<SalesLead> scrapeAuthenticated(ScraperConfig config) {
        List<SalesLead> leads = new ArrayList<>();
        try {
            Document doc = buildConnection(LINKEDIN_JOBS_URL, config).get();

            // TODO: update selectors once headless rendering is supported.
            //       The current selectors mirror the guest path; they may work
            //       partially when a valid li_at cookie is present.
            Elements cards = doc.select("div.base-card, div.job-card-container");
            for (Element card : cards) {
                String title   = text(card, "h3.base-search-card__title, span.job-card-list__title");
                String company = text(card, "h4.base-search-card__subtitle, span.job-card-container__company-name");
                String url     = attr(card, "a.base-card__full-link, a.job-card-list__title", "href");

                SalesLead lead = new SalesLead();
                lead.setName(title.isBlank() ? company : title);
                lead.setCompany(company);
                lead.setWebsite(url);
                lead.setSource(getName());
                leads.add(lead);
            }
        } catch (IOException e) {
            log.error("LinkedInLeadFinder (authenticated) failed", e);
        }
        return leads;
    }

    private Connection buildConnection(String url, ScraperConfig config) {
        Connection conn = Jsoup.connect(url)
                .userAgent(config.getUserAgent())
                .timeout(config.getTimeoutMs())
                .followRedirects(true);
        config.getCookies().forEach(conn::cookie);
        return conn;
    }

    private String text(Element parent, String cssQuery) {
        Element el = parent.selectFirst(cssQuery);
        return el != null ? el.text().trim() : "";
    }

    private String attr(Element parent, String cssQuery, String attrName) {
        Element el = parent.selectFirst(cssQuery);
        return el != null ? el.attr(attrName) : "";
    }
}

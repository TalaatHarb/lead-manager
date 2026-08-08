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
 * Discovers sales leads from the
 * <a href="https://www.ycombinator.com/jobs">Y Combinator Jobs</a> board.
 *
 * <p>YC's job board lists companies from the current and recent batches that are
 * actively hiring, making it a strong source of high-growth startup leads.
 *
 * <h3>Guest mode</h3>
 * The public jobs page at {@code https://www.ycombinator.com/jobs} renders job
 * listings in static HTML, so jsoup can parse them without authentication.
 *
 * <h3>Authenticated mode</h3>
 * Not required for the public jobs page.  If you supply a {@code _hc_session}
 * cookie via {@link ScraperConfig} it will be forwarded, which may unlock
 * additional company data on the Work at a Startup portal
 * ({@code https://www.workatastartup.com/jobs}).
 */
public class YCombinatorLeadFinder implements LeadFinder {

    private static final Logger log = LoggerFactory.getLogger(YCombinatorLeadFinder.class);

    /** Public YC jobs listing. */
    private static final String YC_JOBS_URL = "https://www.ycombinator.com/jobs";

    /**
     * Work-at-a-Startup portal — richer data, requires the
     * {@code _hc_session} cookie for full access.
     */
    private static final String WAAS_URL = "https://www.workatastartup.com/jobs";

    @Override
    public String getName() {
        return "Y Combinator Jobs";
    }

    @Override
    public List<SalesLead> findLatestHotLeads(ScraperConfig config) {
        List<SalesLead> leads = new ArrayList<>();

        // Use the richer WAAS portal when authenticated, public page otherwise
        String targetUrl = config.isAuthenticated() ? WAAS_URL : YC_JOBS_URL;

        try {
            Connection conn = Jsoup.connect(targetUrl)
                    .userAgent(config.getUserAgent())
                    .timeout(config.getTimeoutMs())
                    .followRedirects(true);
            config.getCookies().forEach(conn::cookie);

            Document doc = conn.get();
            leads.addAll(parseYcJobsPage(doc));

            if (leads.isEmpty()) {
                log.warn("YCombinatorLeadFinder: no leads parsed from {}. "
                        + "Selectors may need updating if the page structure changed.", targetUrl);
            } else {
                log.info("YCombinatorLeadFinder found {} leads from {}", leads.size(), targetUrl);
            }
        } catch (IOException e) {
            log.error("YCombinatorLeadFinder failed to fetch {}", targetUrl, e);
        }

        return leads;
    }

    /**
     * Parse job listings from the YC jobs page HTML.
     * <p>
     * Selector notes (as of 2024-Q3):
     * <ul>
     *   <li>Each company card is a {@code <div class="company-card">}</li>
     *   <li>Company name is in an {@code <h2>} or {@code <h3>} inside the card</li>
     *   <li>One-liner description is in a {@code <p class="company-blurb">}</li>
     * </ul>
     * Adjust selectors below if YC's markup changes.
     */
    private List<SalesLead> parseYcJobsPage(Document doc) {
        List<SalesLead> leads = new ArrayList<>();

        // Primary selector — YC public jobs page cards
        Elements cards = doc.select("div.company-card");

        // Fallback: Work at a Startup job rows
        if (cards.isEmpty()) {
            cards = doc.select("div.company");
        }

        for (Element card : cards) {
            String company  = firstText(card, "h2", "h3", "a.company-name", "span.company-name");
            String website  = firstAttr(card, "href", "a.company-website", "a[href*='http']");
            String blurb    = firstText(card, "p.company-blurb", "p.blurb", "p");
            String jobTitle = firstText(card, "span.job-name", "span.role", "div.role");

            if (company.isBlank()) {
                continue;
            }

            SalesLead lead = new SalesLead();
            lead.setName(jobTitle.isBlank() ? company : jobTitle);
            lead.setCompany(company);
            lead.setWebsite(website);
            lead.setNotes(blurb);
            lead.setSource(getName());
            leads.add(lead);
        }
        return leads;
    }

    // ---- Helpers ----

    /** Return the text of the first matching selector, or {@code ""}. */
    private String firstText(Element parent, String... selectors) {
        for (String sel : selectors) {
            Element el = parent.selectFirst(sel);
            if (el != null && !el.text().isBlank()) {
                return el.text().trim();
            }
        }
        return "";
    }

    /** Return the value of {@code attrName} from the first matching selector, or {@code ""}. */
    private String firstAttr(Element parent, String attrName, String... selectors) {
        for (String sel : selectors) {
            Element el = parent.selectFirst(sel);
            if (el != null && !el.attr(attrName).isBlank()) {
                return el.attr(attrName);
            }
        }
        return "";
    }
}

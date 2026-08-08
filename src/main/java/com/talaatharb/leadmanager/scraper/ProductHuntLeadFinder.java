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
 * Discovers sales leads from <a href="https://www.producthunt.com">Product Hunt</a>.
 *
 * <p>Product Hunt publishes a daily digest of the most-upvoted new products.
 * Each product represents a potential lead — a startup or indie maker actively
 * shipping and looking for early adopters / partnerships.
 *
 * <h3>Guest mode</h3>
 * The Product Hunt today page ({@code /}) is partially server-side rendered.
 * jsoup can extract product names and links from the static HTML that React
 * emits into the page source, though the exact selectors may change as the
 * site is updated.
 *
 * <h3>Authenticated mode</h3>
 * Supply a {@code ph_session_key} cookie (and optionally {@code _ph_session})
 * via {@link ScraperConfig} to access personalised or private listings.
 *
 * <p><b>Alternative:</b> Product Hunt exposes a GraphQL API at
 * {@code https://api.producthunt.com/v2/api/graphql} which requires an
 * OAuth developer token.  Populate {@link ScraperConfig#getUsername()} with
 * the token and switch the implementation to use the API for more stable results.
 */
public class ProductHuntLeadFinder implements LeadFinder {

    private static final Logger log = LoggerFactory.getLogger(ProductHuntLeadFinder.class);

    private static final String PH_TODAY_URL = "https://www.producthunt.com";

    @Override
    public String getName() {
        return "Product Hunt Today";
    }

    @Override
    public List<SalesLead> findLatestHotLeads(ScraperConfig config) {
        List<SalesLead> leads = new ArrayList<>();
        try {
            Connection conn = Jsoup.connect(PH_TODAY_URL)
                    .userAgent(config.getUserAgent())
                    .timeout(config.getTimeoutMs())
                    .followRedirects(true)
                    // Product Hunt requires an Accept header or it returns 403
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.5");
            config.getCookies().forEach(conn::cookie);

            Document doc = conn.get();
            leads.addAll(parseProductHuntPage(doc));

            if (leads.isEmpty()) {
                log.warn("ProductHuntLeadFinder: no leads parsed. "
                        + "The page may be fully client-side rendered. "
                        + "Consider integrating the Product Hunt GraphQL API "
                        + "(https://api.producthunt.com/v2/api/graphql) for stable results.");
            } else {
                log.info("ProductHuntLeadFinder found {} leads", leads.size());
            }
        } catch (IOException e) {
            log.error("ProductHuntLeadFinder failed", e);
        }
        return leads;
    }

    /**
     * Parse product cards from the Product Hunt landing page.
     * <p>
     * Selector notes (as of 2024-Q3):
     * <ul>
     *   <li>Product cards live inside {@code <section data-test="homepage-section-0">}</li>
     *   <li>Each card is an {@code <li>} element</li>
     *   <li>Product name: {@code span[data-test=product-name]}</li>
     *   <li>Tagline:      {@code span[data-test=product-tagline]}</li>
     *   <li>Link:         {@code a[data-test=post-name]}</li>
     * </ul>
     * Adjust selectors if Product Hunt's markup changes.
     */
    private List<SalesLead> parseProductHuntPage(Document doc) {
        List<SalesLead> leads = new ArrayList<>();

        // Primary selectors (React data-test attributes are more stable than class names)
        Elements products = doc.select("[data-test=post-name]");

        if (products.isEmpty()) {
            // Fallback: try generic article/li cards
            products = doc.select("article a[href^='/posts/']");
        }

        for (Element product : products) {
            String name    = product.text().trim();
            String url     = "https://www.producthunt.com" + product.attr("href");
            String tagline = "";

            // Try to grab the tagline from a sibling element
            Element card = product.closest("li, article, div[class*=item]");
            if (card != null) {
                Element taglineEl = card.selectFirst("[data-test=product-tagline], p");
                if (taglineEl != null) {
                    tagline = taglineEl.text().trim();
                }
            }

            if (name.isBlank()) {
                continue;
            }

            SalesLead lead = new SalesLead();
            lead.setName(name);
            lead.setWebsite(url);
            lead.setNotes(tagline);
            lead.setSource(getName());
            leads.add(lead);
        }
        return leads;
    }
}

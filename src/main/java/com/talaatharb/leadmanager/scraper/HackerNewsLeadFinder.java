package com.talaatharb.leadmanager.scraper;

import com.talaatharb.leadmanager.entity.SalesLead;
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
 * Scrapes the "Who is Hiring?" thread on Hacker News for sales leads.
 * <p>
 * This is a sample implementation of {@link LeadFinder} using jsoup.
 * Adjust the CSS selectors / URL as the HN thread format evolves.
 */
public class HackerNewsLeadFinder implements LeadFinder {

    private static final Logger log = LoggerFactory.getLogger(HackerNewsLeadFinder.class);
    private static final String HN_JOBS_URL = "https://news.ycombinator.com/jobs";

    @Override
    public String getName() {
        return "Hacker News Jobs";
    }

    @Override
    public List<SalesLead> findLatestHotLeads(ScraperConfig config) {
        List<SalesLead> leads = new ArrayList<>();
        try {
            org.jsoup.Connection conn = Jsoup.connect(HN_JOBS_URL)
                    .userAgent(config.getUserAgent())
                    .timeout(config.getTimeoutMs());
            config.getCookies().forEach(conn::cookie);

            Document doc = conn.get();

            Elements jobItems = doc.select("tr.athing");
            for (Element item : jobItems) {
                Element titleEl = item.selectFirst("td.title a");
                if (titleEl == null) {
                    continue;
                }

                String title = titleEl.text();
                String url = titleEl.absUrl("href");

                SalesLead lead = new SalesLead();
                lead.setName(title);
                lead.setWebsite(url);
                lead.setSource(getName());
                leads.add(lead);
            }
            log.info("HackerNewsLeadFinder found {} leads", leads.size());
        } catch (IOException e) {
            log.error("Failed to scrape Hacker News jobs", e);
        }
        return leads;
    }
}

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
 * Scrapes GitHub Jobs / Trending repositories to discover potential leads.
 * <p>
 * A second example {@link LeadFinder} implementation using jsoup.
 */
public class GithubTrendingLeadFinder implements LeadFinder {

    private static final Logger log = LoggerFactory.getLogger(GithubTrendingLeadFinder.class);
    private static final String GITHUB_TRENDING_URL = "https://github.com/trending";

    @Override
    public String getName() {
        return "GitHub Trending";
    }

    @Override
    public List<SalesLead> findLatestHotLeads(ScraperConfig config) {
        List<SalesLead> leads = new ArrayList<>();
        try {
            org.jsoup.Connection conn = Jsoup.connect(GITHUB_TRENDING_URL)
                    .userAgent(config.getUserAgent())
                    .timeout(config.getTimeoutMs());
            config.getCookies().forEach(conn::cookie);

            Document doc = conn.get();

            Elements repos = doc.select("article.Box-row");
            for (Element repo : repos) {
                Element repoLink = repo.selectFirst("h2 a");
                Element authorEl = repo.selectFirst("span.text-normal");

                if (repoLink == null) {
                    continue;
                }

                String repoName = repoLink.text().trim();
                String repoUrl = "https://github.com" + repoLink.attr("href");
                String author = authorEl != null ? authorEl.text().replace("/", "").trim() : "";

                SalesLead lead = new SalesLead();
                lead.setName(repoName);
                lead.setCompany(author);
                lead.setWebsite(repoUrl);
                lead.setSource(getName());
                leads.add(lead);
            }
            log.info("GithubTrendingLeadFinder found {} leads", leads.size());
        } catch (IOException e) {
            log.error("Failed to scrape GitHub Trending", e);
        }
        return leads;
    }
}

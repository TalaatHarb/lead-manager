package com.talaatharb.leadmanager.scraper;

import com.talaatharb.leadmanager.entity.SalesLead;

import java.util.List;

/**
 * Contract that every lead-finding scraper must implement.
 * <p>
 * Implementations can target any website or data source (e.g. LinkedIn,
 * Crunchbase, HackerNews) and return a list of hot sales leads discovered
 * during the latest run.
 * <p>
 * Scrapers that support authenticated sessions should honour the
 * {@link ScraperConfig} passed to {@link #findLatestHotLeads(ScraperConfig)}.
 * A default no-arg overload delegates to that method with a guest config.
 */
public interface LeadFinder {

    /**
     * Scrape or query the underlying source using the supplied configuration
     * and return the latest hot leads.
     *
     * @param config scraper configuration (credentials, cookies, timeouts, …)
     * @return a non-null, possibly empty list of discovered {@link SalesLead} objects
     */
    List<SalesLead> findLatestHotLeads(ScraperConfig config);

    /**
     * Convenience overload using a default guest (anonymous) configuration.
     *
     * @return a non-null, possibly empty list of discovered {@link SalesLead} objects
     */
    default List<SalesLead> findLatestHotLeads() {
        return findLatestHotLeads(ScraperConfig.defaultConfig());
    }

    /**
     * Human-readable name of this lead finder (e.g. "HackerNews Jobs Scraper").
     *
     * @return display name
     */
    String getName();
}

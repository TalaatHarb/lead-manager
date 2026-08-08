package com.talaatharb.leadmanager.scraper;

import com.talaatharb.leadmanager.entity.SalesLead;

import java.util.List;

/**
 * Contract that every lead-finding scraper must implement.
 * <p>
 * Implementations can target any website or data source (e.g. LinkedIn,
 * Crunchbase, HackerNews) and return a list of hot sales leads discovered
 * during the latest run.
 */
public interface LeadFinder {

    /**
     * Scrape or query the underlying source and return the latest hot leads.
     *
     * @return a non-null, possibly empty list of discovered {@link SalesLead} objects
     */
    List<SalesLead> findLatestHotLeads();

    /**
     * Human-readable name of this lead finder (e.g. "HackerNews Jobs Scraper").
     *
     * @return display name
     */
    String getName();
}

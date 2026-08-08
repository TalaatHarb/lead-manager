package com.talaatharb.leadmanager.scripting;

import com.talaatharb.leadmanager.entity.SalesLead;
import com.talaatharb.leadmanager.scraper.LeadFinder;
import com.talaatharb.leadmanager.scraper.ScraperConfig;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A {@link LeadFinder} that delegates to a Groovy script for its logic.
 * <p>
 * The script is evaluated each time {@link #findLatestHotLeads(ScraperConfig)} is called,
 * so changes to the script file are picked up immediately without restarting.
 */
@Getter
public class GroovyLeadFinder implements LeadFinder {

    private static final Logger log = LoggerFactory.getLogger(GroovyLeadFinder.class);

    private final String name;
    @Setter
    private String script;
    private final GroovyScriptRunner runner;

    public GroovyLeadFinder(String name, String script, GroovyScriptRunner runner) {
        this.name = name;
        this.script = script;
        this.runner = runner;
    }

    @Override
    public List<SalesLead> findLatestHotLeads(ScraperConfig config) {
        log.info("Running Groovy lead finder: {}", name);
        return runner.executeLeadFinderScript(script);
    }
}

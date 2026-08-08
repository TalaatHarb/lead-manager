package com.talaatharb.leadmanager.scripting;

import com.talaatharb.leadmanager.entity.SalesLead;
import com.talaatharb.leadmanager.scraper.LeadFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A {@link LeadFinder} that delegates to a Groovy script for its logic.
 * <p>
 * The script is evaluated each time {@link #findLatestHotLeads()} is called,
 * so changes to the script file are picked up immediately without restarting.
 */
public class GroovyLeadFinder implements LeadFinder {

    private static final Logger log = LoggerFactory.getLogger(GroovyLeadFinder.class);

    private final String name;
    private String script;
    private final GroovyScriptRunner runner;

    public GroovyLeadFinder(String name, String script, GroovyScriptRunner runner) {
        this.name = name;
        this.script = script;
        this.runner = runner;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    @Override
    public List<SalesLead> findLatestHotLeads() {
        log.info("Running Groovy lead finder: {}", name);
        return runner.executeLeadFinderScript(script);
    }
}

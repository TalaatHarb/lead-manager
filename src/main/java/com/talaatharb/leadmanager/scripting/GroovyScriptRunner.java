package com.talaatharb.leadmanager.scripting;

import com.talaatharb.leadmanager.entity.SalesLead;
import com.talaatharb.leadmanager.scraper.LeadFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Executes Groovy scripts at runtime, allowing users to write custom
 * {@link LeadFinder} logic without recompiling the application.
 * <p>
 * A Groovy script can access a pre-bound {@code repository} variable and is
 * expected to return a {@code List<SalesLead>} when used as a lead finder.
 */
public class GroovyScriptRunner {

    private static final Logger log = LoggerFactory.getLogger(GroovyScriptRunner.class);

    private final ScriptEngine engine;

    public GroovyScriptRunner() {
        ScriptEngineManager manager = new ScriptEngineManager();
        this.engine = manager.getEngineByName("groovy");
        if (this.engine == null) {
            throw new IllegalStateException(
                    "Groovy ScriptEngine not found – ensure groovy-jsr223 is on the classpath");
        }
        log.info("GroovyScriptRunner initialised");
    }

    /**
     * Execute arbitrary Groovy source code with optional variable bindings.
     *
     * @param script   Groovy source code
     * @param bindings variables available inside the script
     * @return the value returned by the script, or {@code null}
     * @throws ScriptException if the script contains errors
     */
    public Object execute(String script, Map<String, Object> bindings) throws ScriptException {
        javax.script.Bindings scriptBindings = engine.createBindings();
        scriptBindings.putAll(bindings);
        return engine.eval(script, scriptBindings);
    }

    /**
     * Execute a Groovy script that acts as a {@link LeadFinder}.
     * The script must return a {@code List<SalesLead>}.
     *
     * @param script Groovy source code
     * @return leads produced by the script
     */
    @SuppressWarnings("unchecked")
    public List<SalesLead> executeLeadFinderScript(String script) {
        try {
            javax.script.Bindings scriptBindings = engine.createBindings();
            Object result = engine.eval(script, scriptBindings);
            if (result instanceof List<?> list) {
                return (List<SalesLead>) list;
            }
            log.warn("Script did not return a List; got {}", result == null ? "null" : result.getClass());
        } catch (ScriptException e) {
            log.error("Error executing lead-finder Groovy script", e);
        }
        return Collections.emptyList();
    }
}

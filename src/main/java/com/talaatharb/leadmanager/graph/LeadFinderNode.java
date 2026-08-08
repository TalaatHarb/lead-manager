package com.talaatharb.leadmanager.graph;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A single node in a {@link LeadFinderGraph}.
 * <p>
 * Each node has a type that determines what processing it performs
 * (e.g. SCRAPE, FILTER, ENRICH, OUTPUT) and a map of configuration
 * properties specific to that type.
 */
@Getter
@Setter
public class LeadFinderNode implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private String label;
    private NodeType type;
    private final Map<String, String> properties;

    // Visual position hints for the graph editor
    private double x;
    private double y;

    public LeadFinderNode(String label, NodeType type) {
        this(UUID.randomUUID().toString(), label, type);
    }

    public LeadFinderNode(String id, String label, NodeType type) {
        this.id = id;
        this.label = label;
        this.type = type;
        this.properties = new HashMap<>();
    }

    public void setProperty(String key, String value) { properties.put(key, value); }
    public String getProperty(String key) { return properties.get(key); }

    @Override
    public String toString() {
        return label + " [" + type + "]";
    }

    /** Classification of what a node does in the lead-finder pipeline. */
    public enum NodeType {
        /** Fetches raw leads from a website or API. */
        SCRAPE,
        /** Removes unwanted leads based on criteria. */
        FILTER,
        /** Enriches leads with additional data. */
        ENRICH,
        /** Executes a Groovy script on the lead list. */
        SCRIPT,
        /** Persists leads to the repository. */
        OUTPUT
    }
}

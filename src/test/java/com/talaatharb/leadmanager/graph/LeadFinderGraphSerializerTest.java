package com.talaatharb.leadmanager.graph;

import com.talaatharb.leadmanager.graph.LeadFinderNode.NodeType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LeadFinderGraphSerializerTest {

    @Test
    void serializeAndDeserializeGraph() {
        LeadFinderGraph graph = new LeadFinderGraph();
        LeadFinderNode scrape = new LeadFinderNode("Scrape", NodeType.SCRAPE);
        scrape.setX(10);
        scrape.setY(20);
        scrape.setProperty("url", "https://example.com");
        LeadFinderNode script = new LeadFinderNode("Transform", NodeType.SCRIPT);
        script.setX(30);
        script.setY(40);
        script.setProperty("language", "groovy");
        graph.addNode(scrape);
        graph.addNode(script);
        graph.connect(scrape.getId(), script.getId());

        String serialized = graph.serialize();
        LeadFinderGraph restored = LeadFinderGraph.deserialize(serialized);

        assertEquals(2, restored.getNodes().size());
        assertEquals(1, restored.getEdges().size());

        LeadFinderNode restoredScrape = restored.getNode(scrape.getId());
        assertNotNull(restoredScrape);
        assertEquals("Scrape", restoredScrape.getLabel());
        assertEquals(NodeType.SCRAPE, restoredScrape.getType());
        assertEquals(10, restoredScrape.getX());
        assertEquals(20, restoredScrape.getY());
        assertEquals("https://example.com", restoredScrape.getProperty("url"));
    }
}

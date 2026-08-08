package com.talaatharb.leadmanager.graph;

import com.talaatharb.leadmanager.graph.LeadFinderNode.NodeType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LeadFinderGraphTest {

    @Test
    void addAndRetrieveNode() {
        LeadFinderGraph graph = new LeadFinderGraph();
        LeadFinderNode node = new LeadFinderNode("Scrape HN", NodeType.SCRAPE);
        graph.addNode(node);

        assertEquals(1, graph.getNodes().size());
        assertNotNull(graph.getNode(node.getId()));
    }

    @Test
    void connectNodes() {
        LeadFinderGraph graph = new LeadFinderGraph();
        LeadFinderNode a = new LeadFinderNode("A", NodeType.SCRAPE);
        LeadFinderNode b = new LeadFinderNode("B", NodeType.OUTPUT);
        graph.addNode(a);
        graph.addNode(b);
        graph.connect(a.getId(), b.getId());

        assertEquals(1, graph.getEdges().size());
    }

    @Test
    void removeNode() {
        LeadFinderGraph graph = new LeadFinderGraph();
        LeadFinderNode node = new LeadFinderNode("X", NodeType.FILTER);
        graph.addNode(node);
        graph.removeNode(node.getId());

        assertTrue(graph.getNodes().isEmpty());
    }
}

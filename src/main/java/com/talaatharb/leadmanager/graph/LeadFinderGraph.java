package com.talaatharb.leadmanager.graph;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a directed graph of {@link LeadFinderNode} objects that
 * together form a custom lead-finder pipeline.
 * <p>
 * Nodes model processing steps (scrape, filter, enrich, output).
 * Edges define the data-flow order between steps.
 * The graph is backed by JGraphT and can be serialised to/from a compact text format.
 */
public class LeadFinderGraph {

    private static final Logger log = LoggerFactory.getLogger(LeadFinderGraph.class);

    private final DefaultDirectedGraph<LeadFinderNode, DefaultEdge> graph;
    private final Map<String, LeadFinderNode> nodeIndex;

    public LeadFinderGraph() {
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        this.nodeIndex = new HashMap<>();
    }

    /** Add a node to the graph. */
    public void addNode(LeadFinderNode node) {
        graph.addVertex(node);
        nodeIndex.put(node.getId(), node);
        log.debug("Added node {}", node.getId());
    }

    /** Remove a node and all its edges. */
    public void removeNode(String nodeId) {
        LeadFinderNode node = nodeIndex.remove(nodeId);
        if (node != null) {
            graph.removeVertex(node);
            log.debug("Removed node {}", nodeId);
        }
    }

    /** Connect two existing nodes with a directed edge. */
    public void connect(String fromId, String toId) {
        LeadFinderNode from = nodeIndex.get(fromId);
        LeadFinderNode to = nodeIndex.get(toId);
        if (from == null || to == null) {
            throw new IllegalArgumentException(
                    "Both nodes must exist before connecting: " + fromId + " -> " + toId);
        }
        graph.addEdge(from, to);
        log.debug("Connected {} -> {}", fromId, toId);
    }

    /** Remove the edge between two nodes. */
    public void disconnect(String fromId, String toId) {
        LeadFinderNode from = nodeIndex.get(fromId);
        LeadFinderNode to = nodeIndex.get(toId);
        if (from != null && to != null) {
            graph.removeEdge(from, to);
        }
    }

    public Set<LeadFinderNode> getNodes() {
        return graph.vertexSet();
    }

    public Set<DefaultEdge> getEdges() {
        return graph.edgeSet();
    }

    public LeadFinderNode getNode(String id) {
        return nodeIndex.get(id);
    }

    public DefaultDirectedGraph<LeadFinderNode, DefaultEdge> getGraph() {
        return graph;
    }

    public String serialize() {
        return LeadFinderGraphSerializer.serialize(this);
    }

    public static LeadFinderGraph deserialize(String serializedGraph) {
        return LeadFinderGraphSerializer.deserialize(serializedGraph);
    }
}

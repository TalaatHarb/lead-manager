package com.talaatharb.leadmanager.graph;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LeadFinderGraphSerializer {

    private LeadFinderGraphSerializer() {
    }

    public static String serialize(LeadFinderGraph graph) {
        GraphSnapshot snapshot = new GraphSnapshot();
        for (LeadFinderNode node : graph.getNodes()) {
            NodeSnapshot nodeSnapshot = new NodeSnapshot();
            nodeSnapshot.setId(node.getId());
            nodeSnapshot.setLabel(node.getLabel());
            nodeSnapshot.setType(node.getType().name());
            nodeSnapshot.setX(node.getX());
            nodeSnapshot.setY(node.getY());
            nodeSnapshot.setProperties(new HashMap<>(node.getProperties()));
            snapshot.getNodes().add(nodeSnapshot);
        }
        graph.getEdges().forEach(edge -> {
            EdgeSnapshot edgeSnapshot = new EdgeSnapshot();
            edgeSnapshot.setFromId(graph.getGraph().getEdgeSource(edge).getId());
            edgeSnapshot.setToId(graph.getGraph().getEdgeTarget(edge).getId());
            snapshot.getEdges().add(edgeSnapshot);
        });

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (XMLEncoder encoder = new XMLEncoder(outputStream)) {
            encoder.writeObject(snapshot);
            encoder.flush();
        }
        return outputStream.toString(StandardCharsets.UTF_8);
    }

    public static LeadFinderGraph deserialize(String serializedGraph) {
        try (XMLDecoder decoder = new XMLDecoder(
                new ByteArrayInputStream(serializedGraph.getBytes(StandardCharsets.UTF_8)))) {
            GraphSnapshot snapshot = (GraphSnapshot) decoder.readObject();
            LeadFinderGraph graph = new LeadFinderGraph();
            snapshot.getNodes().forEach(nodeSnapshot -> {
                LeadFinderNode node = new LeadFinderNode(
                        nodeSnapshot.getId(),
                        nodeSnapshot.getLabel(),
                        LeadFinderNode.NodeType.valueOf(nodeSnapshot.getType()));
                node.setX(nodeSnapshot.getX());
                node.setY(nodeSnapshot.getY());
                if (nodeSnapshot.getProperties() != null) {
                    node.getProperties().putAll(nodeSnapshot.getProperties());
                }
                graph.addNode(node);
            });
            snapshot.getEdges().forEach(edgeSnapshot ->
                    graph.connect(edgeSnapshot.getFromId(), edgeSnapshot.getToId()));
            return graph;
        }
    }

    public static class GraphSnapshot {
        private List<NodeSnapshot> nodes = new ArrayList<>();
        private List<EdgeSnapshot> edges = new ArrayList<>();

        public List<NodeSnapshot> getNodes() {
            return nodes;
        }

        public void setNodes(List<NodeSnapshot> nodes) {
            this.nodes = nodes;
        }

        public List<EdgeSnapshot> getEdges() {
            return edges;
        }

        public void setEdges(List<EdgeSnapshot> edges) {
            this.edges = edges;
        }
    }

    public static class NodeSnapshot {
        private String id;
        private String label;
        private String type;
        private double x;
        private double y;
        private Map<String, String> properties = new HashMap<>();

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, String> properties) {
            this.properties = properties;
        }
    }

    public static class EdgeSnapshot {
        private String fromId;
        private String toId;

        public String getFromId() {
            return fromId;
        }

        public void setFromId(String fromId) {
            this.fromId = fromId;
        }

        public String getToId() {
            return toId;
        }

        public void setToId(String toId) {
            this.toId = toId;
        }
    }
}

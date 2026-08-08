package com.talaatharb.leadmanager.graph;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

public final class LeadFinderGraphSerializer {

    private static final String HEADER = "LMGF1";

    private LeadFinderGraphSerializer() {
    }

    public static String serialize(LeadFinderGraph graph) {
        StringBuilder builder = new StringBuilder(HEADER).append('\n');
        for (LeadFinderNode node : graph.getNodes()) {
            builder.append("NODE\t")
                    .append(encode(node.getId())).append('\t')
                    .append(encode(node.getLabel())).append('\t')
                    .append(node.getType().name()).append('\t')
                    .append(node.getX()).append('\t')
                    .append(node.getY()).append('\t')
                    .append(serializeProperties(node))
                    .append('\n');
        }
        graph.getEdges().forEach(edge -> builder.append("EDGE\t")
                .append(encode(graph.getGraph().getEdgeSource(edge).getId())).append('\t')
                .append(encode(graph.getGraph().getEdgeTarget(edge).getId()))
                .append('\n'));
        return builder.toString();
    }

    public static LeadFinderGraph deserialize(String serializedGraph) {
        if (serializedGraph == null || serializedGraph.isBlank()) {
            throw new IllegalArgumentException("Serialized graph must not be blank");
        }

        String[] lines = serializedGraph.split("\\R");
        if (lines.length == 0 || !HEADER.equals(lines[0])) {
            throw new IllegalArgumentException("Unsupported graph serialization format");
        }

        LeadFinderGraph graph = new LeadFinderGraph();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.split("\\t", -1);
            if (parts.length == 0) {
                continue;
            }
            switch (parts[0]) {
                case "NODE" -> deserializeNode(graph, parts);
                case "EDGE" -> deserializeEdge(graph, parts);
                default -> throw new IllegalArgumentException("Unsupported graph record: " + parts[0]);
            }
        }
        return graph;
    }

    private static void deserializeNode(LeadFinderGraph graph, String[] parts) {
        if (parts.length != 7) {
            throw new IllegalArgumentException("Malformed node record");
        }

        LeadFinderNode node = new LeadFinderNode(
                decode(parts[1]),
                decode(parts[2]),
                LeadFinderNode.NodeType.valueOf(parts[3]));
        node.setX(Double.parseDouble(parts[4]));
        node.setY(Double.parseDouble(parts[5]));
        deserializeProperties(node, parts[6]);
        graph.addNode(node);
    }

    private static void deserializeEdge(LeadFinderGraph graph, String[] parts) {
        if (parts.length != 3) {
            throw new IllegalArgumentException("Malformed edge record");
        }
        graph.connect(decode(parts[1]), decode(parts[2]));
    }

    private static String serializeProperties(LeadFinderNode node) {
        StringJoiner joiner = new StringJoiner(";");
        node.getProperties().forEach((key, value) ->
                joiner.add(encode(key) + "=" + encode(value)));
        return joiner.toString();
    }

    private static void deserializeProperties(LeadFinderNode node, String serializedProperties) {
        if (serializedProperties == null || serializedProperties.isBlank()) {
            return;
        }
        for (String entry : serializedProperties.split(";")) {
            if (entry.isBlank()) {
                continue;
            }
            String[] keyValue = entry.split("=", 2);
            if (keyValue.length == 2) {
                node.setProperty(decode(keyValue[0]), decode(keyValue[1]));
            }
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}

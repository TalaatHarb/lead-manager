package com.talaatharb.leadmanager.tracking;

import com.talaatharb.leadmanager.entity.LeadFinderDefinition;
import com.talaatharb.leadmanager.entity.LeadFinderDefinition.LeadFinderType;
import com.talaatharb.leadmanager.graph.LeadFinderGraph;
import com.talaatharb.leadmanager.graph.LeadFinderNode;
import com.talaatharb.leadmanager.repository.LeadFinderRepository;
import com.talaatharb.leadmanager.scraper.HackerNewsLeadFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeadFinderTrackerTest {

    @TempDir
    Path tempDir;

    @Test
    void trackJavaScriptAndGraphLeadFinders() {
        try (LeadFinderRepository repository = new LeadFinderRepository(tempDir.resolve("lead-finders.db"));
             LeadFinderTracker tracker = new LeadFinderTracker(repository)) {
            tracker.trackJavaLeadFinder(new HackerNewsLeadFinder());
            tracker.trackGroovyScript("Custom Script", "return []");

            LeadFinderGraph graph = new LeadFinderGraph();
            graph.addNode(new LeadFinderNode("Output", LeadFinderNode.NodeType.OUTPUT));
            tracker.trackGraph("Custom Graph", graph);

            List<LeadFinderDefinition> definitions = tracker.findAll();
            assertEquals(3, definitions.size());

            LeadFinderDefinition javaDefinition = definitions.stream()
                    .filter(definition -> definition.getType() == LeadFinderType.JAVA_CLASS)
                    .findFirst()
                    .orElseThrow();
            assertEquals(HackerNewsLeadFinder.class.getName(), javaDefinition.getClassName());
            assertTrue(javaDefinition.getSourcePath().endsWith("HackerNewsLeadFinder.java"));

            LeadFinderDefinition scriptDefinition = definitions.stream()
                    .filter(definition -> definition.getType() == LeadFinderType.GROOVY_SCRIPT)
                    .findFirst()
                    .orElseThrow();
            assertEquals("return []", scriptDefinition.getScript());

            LeadFinderDefinition graphDefinition = definitions.stream()
                    .filter(definition -> definition.getType() == LeadFinderType.GRAPH)
                    .findFirst()
                    .orElseThrow();
            assertNotNull(graphDefinition.getSerializedGraph());
            assertEquals(1, LeadFinderGraph.deserialize(graphDefinition.getSerializedGraph()).getNodes().size());
        }
    }

    @Test
    void trackingSameNameUpdatesExistingDefinition() {
        try (LeadFinderRepository repository = new LeadFinderRepository(tempDir.resolve("lead-finders-update.db"));
             LeadFinderTracker tracker = new LeadFinderTracker(repository)) {
            tracker.trackGroovyScript("Custom Script", "return [1]");
            tracker.trackGroovyScript("Custom Script", "return [2]");

            List<LeadFinderDefinition> definitions = tracker.findAll();
            assertEquals(1, definitions.size());
            assertEquals("return [2]", definitions.getFirst().getScript());
        }
    }
}

package com.talaatharb.leadmanager.tracking;

import com.talaatharb.leadmanager.entity.LeadFinderDefinition;
import com.talaatharb.leadmanager.entity.LeadFinderDefinition.LeadFinderType;
import com.talaatharb.leadmanager.graph.LeadFinderGraph;
import com.talaatharb.leadmanager.repository.LeadFinderRepository;
import com.talaatharb.leadmanager.scraper.LeadFinder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class LeadFinderTracker implements AutoCloseable {

    private final LeadFinderRepository repository;

    public LeadFinderTracker() {
        this(new LeadFinderRepository());
    }

    public LeadFinderTracker(LeadFinderRepository repository) {
        this.repository = repository;
    }

    public LeadFinderDefinition trackJavaLeadFinder(LeadFinder leadFinder) {
        String sourcePath = resolveSourcePath(leadFinder.getClass());
        return upsert(leadFinder.getName(), LeadFinderType.JAVA_CLASS, definition -> {
            definition.setClassName(leadFinder.getClass().getName());
            definition.setSourcePath(sourcePath);
            definition.setScript(null);
            definition.setSerializedGraph(null);
        });
    }

    public LeadFinderDefinition trackGroovyScript(String name, String script) {
        return trackGroovyScript(name, script, null);
    }

    public LeadFinderDefinition trackGroovyScript(String name, String script, String sourcePath) {
        return upsert(name, LeadFinderType.GROOVY_SCRIPT, definition -> {
            definition.setClassName(null);
            definition.setSourcePath(sourcePath);
            definition.setScript(script);
            definition.setSerializedGraph(null);
        });
    }

    public LeadFinderDefinition trackGraph(String name, LeadFinderGraph graph) {
        return trackGraph(name, graph, null);
    }

    public LeadFinderDefinition trackGraph(String name, LeadFinderGraph graph, String sourcePath) {
        return upsert(name, LeadFinderType.GRAPH, definition -> {
            definition.setClassName(null);
            definition.setSourcePath(sourcePath);
            definition.setScript(null);
            definition.setSerializedGraph(graph.serialize());
        });
    }

    public List<LeadFinderDefinition> findAll() {
        return repository.findAll();
    }

    private LeadFinderDefinition upsert(String name,
                                        LeadFinderType type,
                                        java.util.function.Consumer<LeadFinderDefinition> updater) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
        var existing = repository.findByNameAndType(name, type);

        LeadFinderDefinition definition = existing.orElseGet(() -> new LeadFinderDefinition(name, type));
        definition.setName(name);
        definition.setType(type);
        updater.accept(definition);
        return existing.isPresent() ? repository.update(definition) : repository.save(definition);
    }

    private String resolveSourcePath(Class<?> type) {
        Path relativePath = Path.of("src", "main", "java",
                type.getName().replace('.', '/') + ".java");
        if (Files.exists(relativePath)) {
            return relativePath.toAbsolutePath().normalize().toString();
        }
        return relativePath.toString();
    }

    @Override
    public void close() {
        repository.close();
    }
}

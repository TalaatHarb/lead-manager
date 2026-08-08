package com.talaatharb.leadmanager.repository;

import com.talaatharb.leadmanager.entity.LeadFinderDefinition;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

public class LeadFinderRepository implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(LeadFinderRepository.class);
    private static final String DEFAULT_DB_FILE = System.getProperty("user.home") + "/.lead-manager/lead-finders.db";

    private final DB db;
    private final ConcurrentMap<String, LeadFinderDefinition> leadFindersMap;

    public LeadFinderRepository() {
        this(Path.of(DEFAULT_DB_FILE));
    }

    public LeadFinderRepository(Path dbPath) {
        File dbFile = dbPath.toFile();
        File parent = dbFile.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }

        this.db = DBMaker.fileDB(dbFile)
                .fileMmapEnableIfSupported()
                .transactionEnable()
                .closeOnJvmShutdown()
                .make();

        this.leadFindersMap = db.hashMap("leadFinders", Serializer.STRING, Serializer.JAVA).createOrOpen();
        log.info("LeadFinderRepository opened at {}", dbFile.getAbsolutePath());
    }

    public LeadFinderDefinition save(LeadFinderDefinition definition) {
        definition.setUpdatedAt(LocalDateTime.now());
        leadFindersMap.put(definition.getId(), definition);
        db.commit();
        return definition;
    }

    public LeadFinderDefinition update(LeadFinderDefinition definition) {
        return save(definition);
    }

    public Optional<LeadFinderDefinition> findById(String id) {
        return Optional.ofNullable(leadFindersMap.get(id));
    }

    public List<LeadFinderDefinition> findAll() {
        Collection<LeadFinderDefinition> values = leadFindersMap.values();
        return new ArrayList<>(values);
    }

    public Optional<LeadFinderDefinition> findByNameAndType(String name, LeadFinderDefinition.LeadFinderType type) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
        return leadFindersMap.values().stream()
                .filter(definition -> type == definition.getType())
                .filter(definition -> name.equals(definition.getName()))
                .findFirst();
    }

    public boolean deleteById(String id) {
        boolean removed = leadFindersMap.remove(id) != null;
        if (removed) {
            db.commit();
        }
        return removed;
    }

    public long count() {
        return leadFindersMap.size();
    }

    @Override
    public void close() {
        db.close();
        log.info("LeadFinderRepository closed");
    }
}

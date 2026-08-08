package com.talaatharb.leadmanager.repository;

import com.talaatharb.leadmanager.entity.SalesLead;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

/**
 * MapDB-backed repository providing CRUD operations for {@link SalesLead}.
 * <p>
 * The database file is created in the user's home directory under
 * {@code .lead-manager/leads.db}.
 */
public class LeadRepository implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(LeadRepository.class);
    private static final String DB_FILE = System.getProperty("user.home") + "/.lead-manager/leads.db";

    private final DB db;
    private final ConcurrentMap<String, SalesLead> leadsMap;

    public LeadRepository() {
        File dbFile = new File(DB_FILE);
        dbFile.getParentFile().mkdirs();

        this.db = DBMaker.fileDB(dbFile)
                .fileMmapEnableIfSupported()
                .transactionEnable()
                .closeOnJvmShutdown()
                .make();

        this.leadsMap = db.hashMap("leads", Serializer.STRING, Serializer.JAVA).createOrOpen();
        log.info("LeadRepository opened at {}", DB_FILE);
    }

    /** Persist a new lead. */
    public SalesLead save(SalesLead lead) {
        leadsMap.put(lead.getId(), lead);
        db.commit();
        log.debug("Saved lead {}", lead.getId());
        return lead;
    }

    /** Update an existing lead (same semantics as save). */
    public SalesLead update(SalesLead lead) {
        lead.setUpdatedAt(LocalDateTime.now());
        leadsMap.put(lead.getId(), lead);
        db.commit();
        log.debug("Updated lead {}", lead.getId());
        return lead;
    }

    /** Find a lead by its UUID. */
    public Optional<SalesLead> findById(String id) {
        return Optional.ofNullable(leadsMap.get(id));
    }

    /** Return all leads. */
    public List<SalesLead> findAll() {
        Collection<SalesLead> values = leadsMap.values();
        return new ArrayList<>(values);
    }

    /** Delete a lead by its UUID. Returns {@code true} if it existed. */
    public boolean deleteById(String id) {
        boolean removed = leadsMap.remove(id) != null;
        if (removed) {
            db.commit();
            log.debug("Deleted lead {}", id);
        }
        return removed;
    }

    /** Total number of persisted leads. */
    public long count() {
        return leadsMap.size();
    }

    @Override
    public void close() {
        db.close();
        log.info("LeadRepository closed");
    }
}

package com.talaatharb.leadmanager.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class LeadFinderDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    @Setter(AccessLevel.NONE)
    private String id;
    private String name;
    private LeadFinderType type;
    private String className;
    private String sourcePath;
    private String script;
    private String serializedGraph;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LeadFinderDefinition() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public LeadFinderDefinition(String name, LeadFinderType type) {
        this();
        this.name = name;
        this.type = type;
    }

    public enum LeadFinderType {
        JAVA_CLASS,
        GROOVY_SCRIPT,
        GRAPH
    }
}

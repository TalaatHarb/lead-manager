package com.talaatharb.leadmanager.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a sales lead entity persisted in MapDB.
 */
@Getter
@Setter
public class SalesLead implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String company;
    private String email;
    private String phone;
    private String website;
    private String source;
    private LeadStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SalesLead() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = LeadStatus.NEW;
    }

    public SalesLead(String name, String company, String email) {
        this();
        this.name = name;
        this.company = company;
        this.email = email;
    }

    @Override
    public String toString() {
        return "SalesLead{id='" + id + "', name='" + name + "', company='" + company
                + "', status=" + status + "}";
    }

    /** Lifecycle stages of a sales lead. */
    public enum LeadStatus {
        NEW, CONTACTED, QUALIFIED, PROPOSAL, NEGOTIATION, WON, LOST
    }
}

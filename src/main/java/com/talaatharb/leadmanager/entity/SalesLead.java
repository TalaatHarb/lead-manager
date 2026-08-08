package com.talaatharb.leadmanager.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a sales lead entity persisted in MapDB.
 */
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

    // --- Getters and Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LeadStatus getStatus() { return status; }
    public void setStatus(LeadStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

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

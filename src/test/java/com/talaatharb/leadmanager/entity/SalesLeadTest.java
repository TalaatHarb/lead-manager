package com.talaatharb.leadmanager.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SalesLeadTest {

    @Test
    void defaultConstructorSetsDefaults() {
        SalesLead lead = new SalesLead();
        assertNotNull(lead.getId());
        assertEquals(SalesLead.LeadStatus.NEW, lead.getStatus());
        assertNotNull(lead.getCreatedAt());
        assertNotNull(lead.getUpdatedAt());
    }

    @Test
    void convenienceConstructorSetsFields() {
        SalesLead lead = new SalesLead("Alice", "Acme", "alice@acme.com");
        assertEquals("Alice", lead.getName());
        assertEquals("Acme", lead.getCompany());
        assertEquals("alice@acme.com", lead.getEmail());
    }

    @Test
    void statusCanBeChanged() {
        SalesLead lead = new SalesLead();
        lead.setStatus(SalesLead.LeadStatus.WON);
        assertEquals(SalesLead.LeadStatus.WON, lead.getStatus());
    }
}

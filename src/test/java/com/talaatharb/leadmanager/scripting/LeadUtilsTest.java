package com.talaatharb.leadmanager.scripting;

import com.talaatharb.leadmanager.entity.SalesLead;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeadUtilsTest {

    @Test
    void deduplicateUsesEmailAndKeepsFirstLead() {
        SalesLead first = new SalesLead("Jane Doe", "Acme", "JANE@ACME.COM");
        SalesLead second = new SalesLead("Jane Different", "Other", "jane@acme.com");

        List<SalesLead> result = LeadUtils.deduplicate(List.of(first, second));

        assertEquals(1, result.size());
        assertEquals(first.getId(), result.getFirst().getId());
    }

    @Test
    void deduplicateFallsBackToIdentityWhenEmailMissing() {
        SalesLead first = new SalesLead(" Alex Smith ", "Beta Labs", null);
        first.setWebsite("https://beta.example");
        SalesLead second = new SalesLead("alex smith", "BETA LABS", null);
        second.setWebsite("https://beta.example");

        List<SalesLead> result = LeadUtils.deduplicate(List.of(first, second));

        assertEquals(1, result.size());
        assertEquals(first.getId(), result.getFirst().getId());
    }

    @Test
    void deduplicateReturnsEmptyForNullInput() {
        assertTrue(LeadUtils.deduplicate(null).isEmpty());
    }
}

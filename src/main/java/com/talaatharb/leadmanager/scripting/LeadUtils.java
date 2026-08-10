package com.talaatharb.leadmanager.scripting;

import com.talaatharb.leadmanager.entity.SalesLead;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Utility helpers for working with lead collections in Java and Groovy scripts.
 */
public final class LeadUtils {

    private LeadUtils() {
    }

    /**
     * Remove duplicate leads while preserving insertion order.
     * <p>
     * Duplicate detection uses normalized email first; when email is missing,
     * it falls back to a normalized identity tuple (name, company, website, phone).
     * All tuple segments must match to be considered duplicates.
     *
     * @param leads input leads
     * @return de-duplicated leads
     */
    public static List<SalesLead> deduplicate(Collection<SalesLead> leads) {
        if (leads == null || leads.isEmpty()) {
            return List.of();
        }

        Map<String, SalesLead> unique = new LinkedHashMap<>();
        for (SalesLead lead : leads) {
            if (lead == null) {
                continue;
            }
            unique.putIfAbsent(buildKey(lead), lead);
        }
        return new ArrayList<>(unique.values());
    }

    private static String buildKey(SalesLead lead) {
        String email = normalize(lead.getEmail());
        if (!email.isEmpty()) {
            return "email:" + email;
        }

        String name = normalize(lead.getName());
        String company = normalize(lead.getCompany());
        String website = normalize(lead.getWebsite());
        String phone = normalize(lead.getPhone());
        if (!name.isEmpty() || !company.isEmpty() || !website.isEmpty() || !phone.isEmpty()) {
            return "identity:"
                    + encode(name) + "|"
                    + encode(company) + "|"
                    + encode(website) + "|"
                    + encode(phone);
        }

        return "id:" + lead.getId();
    }

    private static String encode(String value) {
        return value.length() + ":" + value;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}

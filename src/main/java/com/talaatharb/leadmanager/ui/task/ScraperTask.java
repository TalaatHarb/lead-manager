package com.talaatharb.leadmanager.ui.task;

import com.talaatharb.leadmanager.entity.SalesLead;
import com.talaatharb.leadmanager.scraper.LeadFinder;
import com.talaatharb.leadmanager.scraper.ScraperConfig;
import javafx.concurrent.Task;

import java.util.List;

/**
 * A JavaFX {@link Task} that runs a {@link LeadFinder} on a background thread.
 * <p>
 * Progress, cancellation, and error reporting are surfaced through the standard
 * {@link Task} API so the UI can bind to {@link #progressProperty()},
 * {@link #messageProperty()}, and {@link #exceptionProperty()} without
 * any manual {@code Platform.runLater} calls in the controller.
 */
public class ScraperTask extends Task<List<SalesLead>> {

    private final LeadFinder finder;
    private final ScraperConfig config;

    public ScraperTask(LeadFinder finder, ScraperConfig config) {
        this.finder = finder;
        this.config = config;
        updateTitle(finder.getName());
        updateMessage("Waiting to start\u2026");
    }

    @Override
    protected List<SalesLead> call() {
        updateMessage("Running: " + finder.getName() + " \u2026");
        updateProgress(-1, 1); // indeterminate while running
        List<SalesLead> results = finder.findLatestHotLeads(config);
        if (isCancelled()) {
            updateMessage("Cancelled.");
            return List.of();
        }
        updateProgress(1, 1);
        updateMessage("Done. Found " + results.size() + " leads.");
        return results;
    }
}

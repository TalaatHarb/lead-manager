package com.talaatharb.leadmanager.ui.controller;

import com.talaatharb.leadmanager.entity.SalesLead;
import com.talaatharb.leadmanager.repository.LeadRepository;
import com.talaatharb.leadmanager.scraper.GithubTrendingLeadFinder;
import com.talaatharb.leadmanager.scraper.HackerNewsLeadFinder;
import com.talaatharb.leadmanager.scraper.LeadFinder;
import com.talaatharb.leadmanager.scraper.LinkedInLeadFinder;
import com.talaatharb.leadmanager.scraper.ProductHuntLeadFinder;
import com.talaatharb.leadmanager.scraper.ScraperConfig;
import com.talaatharb.leadmanager.scraper.WellfoundLeadFinder;
import com.talaatharb.leadmanager.scraper.YCombinatorLeadFinder;
import com.talaatharb.leadmanager.scripting.GroovyScriptRunner;
import com.talaatharb.leadmanager.service.BackgroundTaskService;
import com.talaatharb.leadmanager.tracking.LeadFinderTracker;
import com.talaatharb.leadmanager.ui.task.ScraperTask;
import com.talaatharb.leadmanager.ui.view.CodeEditorView;
import com.talaatharb.leadmanager.ui.view.GraphEditorView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Main application controller wiring together all views and services.
 */
public class MainController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @FXML private TabPane mainTabPane;

    @FXML private TableView<SalesLead> leadsTable;
    @FXML private TableColumn<SalesLead, String> colName;
    @FXML private TableColumn<SalesLead, String> colCompany;
    @FXML private TableColumn<SalesLead, String> colEmail;
    @FXML private TableColumn<SalesLead, String> colStatus;
    @FXML private TableColumn<SalesLead, String> colSource;

    @FXML private TextField tfName;
    @FXML private TextField tfCompany;
    @FXML private TextField tfEmail;
    @FXML private TextField tfPhone;
    @FXML private TextField tfWebsite;
    @FXML private TextField tfSource;
    @FXML private ComboBox<SalesLead.LeadStatus> cbStatus;
    @FXML private TextArea taNotes;

    @FXML private ComboBox<String> cbScraper;
    @FXML private TextArea taScraperLog;
    @FXML private Button btnCancelScraper;
    @FXML private ProgressIndicator scraperProgress;

    @FXML private BorderPane editorPane;
    @FXML private BorderPane graphPane;

    @FXML private Label statusBar;

    private LeadRepository repository;
    private GroovyScriptRunner scriptRunner;
    private LeadFinderTracker leadFinderTracker;
    private BackgroundTaskService taskService;
    private ScraperTask activeScraperTask;
    private final ObservableList<SalesLead> leadData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        repository = new LeadRepository();
        scriptRunner = new GroovyScriptRunner();
        leadFinderTracker = new LeadFinderTracker();
        taskService = new BackgroundTaskService();

        setupLeadsTable();
        setupStatusCombo();
        setupScraperCombo();
        setupCodeEditor();
        setupGraphEditor();
        registerBuiltInLeadFinders();
        loadLeads();
    }

    private void setupLeadsTable() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCompany.setCellValueFactory(new PropertyValueFactory<>("company"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colSource.setCellValueFactory(new PropertyValueFactory<>("source"));

        leadsTable.setItems(leadData);
        leadsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> populateForm(selected));
    }

    private void setupStatusCombo() {
        cbStatus.setItems(FXCollections.observableArrayList(SalesLead.LeadStatus.values()));
        cbStatus.setValue(SalesLead.LeadStatus.NEW);
    }

    private void setupScraperCombo() {
        cbScraper.setItems(FXCollections.observableArrayList(
                "Hacker News Jobs",
                "GitHub Trending",
                "LinkedIn Jobs",
                "Y Combinator Jobs",
                "Product Hunt Today",
                "Wellfound (AngelList)"));
        cbScraper.setValue("Hacker News Jobs");
    }

    private void setupCodeEditor() {
        if (editorPane != null) {
            editorPane.setCenter(new CodeEditorView(scriptRunner, leadFinderTracker, taskService));
        }
    }

    private void setupGraphEditor() {
        if (graphPane != null) {
            graphPane.setCenter(new GraphEditorView(leadFinderTracker));
        }
    }

    private void registerBuiltInLeadFinders() {
        builtInLeadFinders().forEach(leadFinderTracker::trackJavaLeadFinder);
    }

    private List<LeadFinder> builtInLeadFinders() {
        return List.of(
                new HackerNewsLeadFinder(),
                new GithubTrendingLeadFinder(),
                new LinkedInLeadFinder(),
                new YCombinatorLeadFinder(),
                new ProductHuntLeadFinder(),
                new WellfoundLeadFinder());
    }

    @FXML
    private void handleSave() {
        SalesLead selected = leadsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            fillFromForm(selected);
            repository.update(selected);
            log.info("Updated lead {}", selected.getId());
        } else {
            SalesLead lead = new SalesLead();
            fillFromForm(lead);
            repository.save(lead);
            leadData.add(lead);
            log.info("Saved new lead {}", lead.getId());
        }
        loadLeads();
        clearForm();
    }

    @FXML
    private void handleDelete() {
        SalesLead selected = leadsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a lead to delete.");
            return;
        }
        repository.deleteById(selected.getId());
        leadData.remove(selected);
        clearForm();
        log.info("Deleted lead {}", selected.getId());
    }

    @FXML
    private void handleNew() {
        leadsTable.getSelectionModel().clearSelection();
        clearForm();
    }

    @FXML
    private void handleRunScraper() {
        if (activeScraperTask != null && activeScraperTask.isRunning()) {
            taScraperLog.appendText("A scraper is already running. Cancel it first.\n");
            return;
        }

        String scraperName = cbScraper.getValue();
        LeadFinder finder = switch (scraperName) {
            case "GitHub Trending" -> new GithubTrendingLeadFinder();
            case "LinkedIn Jobs" -> new LinkedInLeadFinder();
            case "Y Combinator Jobs" -> new YCombinatorLeadFinder();
            case "Product Hunt Today" -> new ProductHuntLeadFinder();
            case "Wellfound (AngelList)" -> new WellfoundLeadFinder();
            default -> new HackerNewsLeadFinder();
        };

        ScraperConfig config = ScraperConfig.defaultConfig();
        ScraperTask task = new ScraperTask(finder, config);
        activeScraperTask = task;

        // Unbind from any previous task before rebinding
        scraperProgress.visibleProperty().unbind();
        scraperProgress.progressProperty().unbind();
        btnCancelScraper.disableProperty().unbind();
        if (statusBar != null) {
            statusBar.textProperty().unbind();
        }

        // Bind scraper progress indicator and cancel button
        scraperProgress.visibleProperty().bind(task.runningProperty());
        scraperProgress.progressProperty().bind(task.progressProperty());
        btnCancelScraper.disableProperty().bind(task.runningProperty().not());
        if (statusBar != null) {
            statusBar.textProperty().bind(task.messageProperty());
        }

        task.setOnScheduled(e ->
                taScraperLog.appendText("Scheduled: " + finder.getName() + "\n"));

        task.setOnSucceeded(e -> {
            List<SalesLead> found = task.getValue();
            found.forEach(lead -> {
                repository.save(lead);
                leadData.add(lead);
                taScraperLog.appendText("  + " + lead.getName() + " [" + lead.getWebsite() + "]\n");
            });
            taScraperLog.appendText("Done. Found " + found.size() + " leads.\n");
            if (statusBar != null) {
                statusBar.textProperty().unbind();
                statusBar.setText("Done. Found " + found.size() + " leads.");
            }
            scraperProgress.progressProperty().unbind();
            scraperProgress.visibleProperty().unbind();
            scraperProgress.setVisible(false);
            scraperProgress.setProgress(0);
            btnCancelScraper.disableProperty().unbind();
            btnCancelScraper.setDisable(true);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            log.error("Scraper '{}' failed", finder.getName(), ex);
            taScraperLog.appendText("ERROR: " + (ex != null ? ex.getMessage() : "unknown error") + "\n");
            if (statusBar != null) {
                statusBar.textProperty().unbind();
                statusBar.setText("Scraper failed.");
            }
            scraperProgress.progressProperty().unbind();
            scraperProgress.visibleProperty().unbind();
            scraperProgress.setVisible(false);
            scraperProgress.setProgress(0);
            btnCancelScraper.disableProperty().unbind();
            btnCancelScraper.setDisable(true);
        });

        task.setOnCancelled(e -> {
            taScraperLog.appendText("Cancelled: " + finder.getName() + "\n");
            if (statusBar != null) {
                statusBar.textProperty().unbind();
                statusBar.setText("Scraper cancelled.");
            }
            scraperProgress.progressProperty().unbind();
            scraperProgress.visibleProperty().unbind();
            scraperProgress.setVisible(false);
            scraperProgress.setProgress(0);
            btnCancelScraper.disableProperty().unbind();
            btnCancelScraper.setDisable(true);
        });

        taskService.submit(task);
    }

    @FXML
    private void handleCancelScraper() {
        if (activeScraperTask != null) {
            activeScraperTask.cancel();
        }
    }

    private void loadLeads() {
        leadData.setAll(repository.findAll());
    }

    private void populateForm(SalesLead lead) {
        if (lead == null) return;
        tfName.setText(lead.getName());
        tfCompany.setText(lead.getCompany());
        tfEmail.setText(lead.getEmail());
        tfPhone.setText(lead.getPhone());
        tfWebsite.setText(lead.getWebsite());
        tfSource.setText(lead.getSource());
        cbStatus.setValue(lead.getStatus());
        taNotes.setText(lead.getNotes());
    }

    private void fillFromForm(SalesLead lead) {
        lead.setName(tfName.getText());
        lead.setCompany(tfCompany.getText());
        lead.setEmail(tfEmail.getText());
        lead.setPhone(tfPhone.getText());
        lead.setWebsite(tfWebsite.getText());
        lead.setSource(tfSource.getText());
        lead.setStatus(cbStatus.getValue());
        lead.setNotes(taNotes.getText());
    }

    private void clearForm() {
        tfName.clear();
        tfCompany.clear();
        tfEmail.clear();
        tfPhone.clear();
        tfWebsite.clear();
        tfSource.clear();
        cbStatus.setValue(SalesLead.LeadStatus.NEW);
        taNotes.clear();
    }

    @FXML
    private void handleExit() {
        if (activeScraperTask != null) {
            activeScraperTask.cancel();
        }
        if (taskService != null) {
            taskService.close();
        }
        if (leadFinderTracker != null) {
            leadFinderTracker.close();
        }
        if (repository != null) {
            repository.close();
        }
        javafx.application.Platform.exit();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }
}

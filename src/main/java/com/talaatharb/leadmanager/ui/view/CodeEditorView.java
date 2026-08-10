package com.talaatharb.leadmanager.ui.view;

import com.talaatharb.leadmanager.scripting.GroovyScriptRunner;
import com.talaatharb.leadmanager.tracking.LeadFinderTracker;
import com.talaatharb.leadmanager.service.BackgroundTaskService;
import com.talaatharb.leadmanager.ui.GroovySyntaxHighlighter;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.util.Collections;

/**
 * A basic code-editor pane built on RichTextFX's {@link CodeArea}.
 * <p>
 * The editor supports Groovy scripting and provides a Run button that
 * executes the script via {@link GroovyScriptRunner}.
 */
public class CodeEditorView extends BorderPane {

    private static final Logger log = LoggerFactory.getLogger(CodeEditorView.class);

    private final CodeArea codeArea;
    private final TextArea outputArea;
    private final GroovyScriptRunner scriptRunner;
    private final LeadFinderTracker tracker;
    private final BackgroundTaskService taskService;

    public CodeEditorView(GroovyScriptRunner scriptRunner, LeadFinderTracker tracker,
                          BackgroundTaskService taskService) {
        this.scriptRunner = scriptRunner;
        this.tracker = tracker;
        this.taskService = taskService;

        TextField finderNameField = new TextField("Custom Groovy Lead Finder");
        finderNameField.setPrefColumnCount(24);

        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setStyle("-fx-font-family: monospace; -fx-font-size: 13;");
        codeArea.replaceText(0, 0, getSampleScript());
        codeArea.textProperty().addListener((obs, oldText, newText) -> applyHighlighting());
        applyHighlighting();

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setMaxHeight(150);
        outputArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12;");

        Button runBtn = new Button("▶ Run");
        runBtn.setOnAction(e -> runScript());
        Button trackBtn = new Button("Track");
        trackBtn.setOnAction(e -> trackScript(finderNameField.getText()));
        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> outputArea.clear());

        HBox toolbar = new HBox(8,
                new Label("Groovy Script Editor"),
                new Label("Name:"), finderNameField,
                runBtn, trackBtn, clearBtn);
        toolbar.setPadding(new Insets(6, 8, 6, 8));

        setTop(toolbar);
        setCenter(codeArea);
        setBottom(outputArea);
    }

    private void runScript() {
        String code = codeArea.getText();
        Task<Object> scriptTask = new Task<>() {
            @Override
            protected Object call() throws ScriptException {
                return scriptRunner.execute(code, Collections.emptyMap());
            }
        };
        scriptTask.setOnSucceeded(e ->
                outputArea.appendText("Result: " + scriptTask.getValue() + "\n"));
        scriptTask.setOnFailed(e -> {
            Throwable ex = scriptTask.getException();
            log.error("Script execution error", ex);
            outputArea.appendText("ERROR: " + (ex != null ? ex.getMessage() : "unknown error") + "\n");
        });
        taskService.submit(scriptTask);
    }

    private void trackScript(String name) {
        tracker.trackGroovyScript(name, codeArea.getText());
        outputArea.appendText("Tracked Groovy lead finder: " + name + "\n");
    }

    private void applyHighlighting() {
        codeArea.setStyleSpans(0, GroovySyntaxHighlighter.computeHighlighting(codeArea.getText()));
    }

    private String getSampleScript() {
        return """
                // Sample Groovy script with JSoup scraping + de-duplication
                import com.talaatharb.leadmanager.entity.SalesLead
                import com.talaatharb.leadmanager.scripting.LeadUtils
                import org.jsoup.Jsoup

                def toLead = { card ->
                    def lead = new SalesLead(
                            card.selectFirst(".name")?.text(),
                            card.selectFirst(".company")?.text(),
                            card.selectFirst(".email")?.text()
                    )
                    lead.website = card.selectFirst(".website a")?.attr("href")
                    lead.source = "Groovy Script Sample"
                    lead
                }

                // Keep this sample runnable offline by parsing embedded HTML.
                // For live scraping, replace Jsoup.parse(...) with Jsoup.connect(url).get().
                def html = '''
                    <section class="lead">
                      <span class="name">Jane Doe</span>
                      <span class="company">Acme Corp</span>
                      <span class="email">jane@acme.com</span>
                      <span class="website"><a href="https://acme.example">Website</a></span>
                    </section>
                    <section class="lead">
                      <span class="name">Jane Doe</span>
                      <span class="company">Acme Corp</span>
                      <span class="email">jane@acme.com</span>
                      <span class="website"><a href="https://acme.example">Website</a></span>
                    </section>
                    <section class="lead">
                      <span class="name">Alex Smith</span>
                      <span class="company">Beta Labs</span>
                      <span class="email">alex@betalabs.io</span>
                      <span class="website"><a href="https://betalabs.example">Website</a></span>
                    </section>
                '''

                def doc = Jsoup.parse(html)
                def leads = doc.select("section.lead").collect(toLead)
                def deduplicatedLeads = LeadUtils.deduplicate(leads)
                println "Scraped ${leads.size()} leads, unique: ${deduplicatedLeads.size()}"
                deduplicatedLeads
                """;
    }

    public CodeArea getCodeArea() {
        return codeArea;
    }
}

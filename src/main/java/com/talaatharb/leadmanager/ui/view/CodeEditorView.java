package com.talaatharb.leadmanager.ui.view;

import com.talaatharb.leadmanager.scripting.GroovyScriptRunner;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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

    public CodeEditorView(GroovyScriptRunner scriptRunner) {
        this.scriptRunner = scriptRunner;

        // --- Code area ---
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setStyle("-fx-font-family: monospace; -fx-font-size: 13;");
        codeArea.replaceText(0, 0, getSampleScript());

        // --- Output area ---
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setMaxHeight(150);
        outputArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12;");

        // --- Toolbar ---
        Button runBtn = new Button("▶ Run");
        runBtn.setOnAction(e -> runScript());
        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> outputArea.clear());

        HBox toolbar = new HBox(8, new Label("Groovy Script Editor"), runBtn, clearBtn);
        toolbar.setPadding(new Insets(6, 8, 6, 8));

        setTop(toolbar);
        setCenter(codeArea);
        setBottom(outputArea);
    }

    private void runScript() {
        String code = codeArea.getText();
        try {
            Object result = scriptRunner.execute(code, Collections.emptyMap());
            outputArea.appendText("Result: " + result + "\n");
        } catch (ScriptException ex) {
            log.error("Script execution error", ex);
            outputArea.appendText("ERROR: " + ex.getMessage() + "\n");
        }
    }

    private String getSampleScript() {
        return """
                // Sample Groovy script – returns a list of lead names
                import com.talaatharb.leadmanager.entity.SalesLead

                def lead = new SalesLead("John Doe", "Acme Corp", "john@acme.com")
                println "Created: ${lead}"
                [lead]
                """;
    }

    public CodeArea getCodeArea() {
        return codeArea;
    }
}

package com.talaatharb.leadmanager.ui.view;

import com.talaatharb.leadmanager.graph.LeadFinderGraph;
import com.talaatharb.leadmanager.graph.LeadFinderNode;
import com.talaatharb.leadmanager.graph.LeadFinderNode.NodeType;
import com.talaatharb.leadmanager.tracking.LeadFinderTracker;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A rudimentary graph-editor pane for building custom lead-finder pipelines.
 * <p>
 * Nodes are represented as draggable circles on a {@link Pane} canvas.
 * The graph model is maintained by {@link LeadFinderGraph} (JGraphT).
 * <p>
 * This is a skeleton: a production implementation would use a full
 * node-graph UI library or a custom JavaFX canvas renderer.
 */
public class GraphEditorView extends BorderPane {

    private static final Logger log = LoggerFactory.getLogger(GraphEditorView.class);

    @Getter
    private final LeadFinderGraph graphModel = new LeadFinderGraph();
    private final Pane canvas = new Pane();
    private final LeadFinderTracker tracker;
    private final Label statusLabel = new Label("Ready");

    public GraphEditorView(LeadFinderTracker tracker) {
        this.tracker = tracker;

        ComboBox<NodeType> nodeTypeCombo = new ComboBox<>();
        nodeTypeCombo.getItems().addAll(NodeType.values());
        nodeTypeCombo.setValue(NodeType.SCRAPE);

        TextField labelField = new TextField("New Node");
        TextField finderNameField = new TextField("Custom Graph Lead Finder");
        finderNameField.setPrefColumnCount(22);

        Button addNodeBtn = new Button("Add Node");
        addNodeBtn.setOnAction(e -> addNodeToCanvas(labelField.getText(), nodeTypeCombo.getValue()));

        Button trackBtn = new Button("Track Graph");
        trackBtn.setOnAction(e -> trackGraph(finderNameField.getText()));

        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> clearCanvas());

        HBox toolbar = new HBox(8, new Label("Type:"), nodeTypeCombo,
                new Label("Label:"), labelField,
                new Label("Name:"), finderNameField,
                addNodeBtn, trackBtn, clearBtn);
        toolbar.setPadding(new Insets(6, 8, 6, 8));

        canvas.setStyle("-fx-background-color: #f4f4f4;");
        ScrollPane scroll = new ScrollPane(canvas);
        scroll.setPannable(true);

        BorderPane.setMargin(statusLabel, new Insets(6, 8, 6, 8));
        setTop(toolbar);
        setCenter(scroll);
        setBottom(statusLabel);

        addStarterNodes();
    }

    private void addStarterNodes() {
        addNodeToCanvas("Scrape HN", NodeType.SCRAPE, 80, 100);
        addNodeToCanvas("Filter B2B", NodeType.FILTER, 280, 100);
        addNodeToCanvas("Save", NodeType.OUTPUT, 480, 100);
    }

    private void addNodeToCanvas(String label, NodeType type) {
        double x = 50 + Math.random() * 600;
        double y = 50 + Math.random() * 300;
        addNodeToCanvas(label, type, x, y);
    }

    private void addNodeToCanvas(String label, NodeType type, double x, double y) {
        LeadFinderNode node = new LeadFinderNode(label, type);
        node.setX(x);
        node.setY(y);
        graphModel.addNode(node);

        Circle circle = new Circle(30, nodeColor(type));
        circle.setStroke(Color.DARKGRAY);
        circle.setStrokeWidth(1.5);

        Text text = new Text(label);
        text.setStyle("-fx-font-size: 10;");

        StackPane nodePane = new StackPane(circle, text);
        nodePane.setLayoutX(x - 30);
        nodePane.setLayoutY(y - 30);

        enableDrag(nodePane, node);

        Tooltip.install(nodePane, new Tooltip(type.name() + ": " + label));
        canvas.getChildren().add(nodePane);
        log.debug("Added node '{}' of type {} at ({}, {})", label, type, x, y);
    }

    private void clearCanvas() {
        canvas.getChildren().clear();
        List<String> ids = graphModel.getNodes().stream()
                .map(LeadFinderNode::getId)
                .toList();
        ids.forEach(graphModel::removeNode);
        statusLabel.setText("Graph cleared");
        log.debug("Canvas cleared");
    }

    private void trackGraph(String name) {
        tracker.trackGraph(name, graphModel);
        statusLabel.setText("Tracked graph lead finder: " + name);
    }

    /** Make a node pane draggable on the canvas. */
    private void enableDrag(StackPane pane, LeadFinderNode node) {
        final double[] dragDelta = new double[2];
        pane.setOnMousePressed(e -> {
            dragDelta[0] = pane.getLayoutX() - e.getSceneX();
            dragDelta[1] = pane.getLayoutY() - e.getSceneY();
        });
        pane.setOnMouseDragged(e -> {
            double layoutX = e.getSceneX() + dragDelta[0];
            double layoutY = e.getSceneY() + dragDelta[1];
            pane.setLayoutX(layoutX);
            pane.setLayoutY(layoutY);
            node.setX(layoutX + 30);
            node.setY(layoutY + 30);
        });
    }

    private Color nodeColor(NodeType type) {
        return switch (type) {
            case SCRAPE -> Color.STEELBLUE;
            case FILTER -> Color.GOLDENROD;
            case ENRICH -> Color.MEDIUMSEAGREEN;
            case SCRIPT -> Color.MEDIUMPURPLE;
            case OUTPUT -> Color.TOMATO;
        };
    }
}

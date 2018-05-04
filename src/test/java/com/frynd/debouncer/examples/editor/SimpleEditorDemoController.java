package com.frynd.debouncer.examples.editor;

import com.frynd.debouncer.Debouncer;
import com.frynd.debouncer.accumulator.impl.LatestValueAccumulator;
import com.frynd.debouncer.regulator.impl.QuiescentRegulator;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextInputControl;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * Simple editor fxml controller
 * <br/>
 * Loads a template file: "editor/template.html" which has a {@code %s} format string.
 * Replaces the %s using with the text input by the user.
 * Displays the effective text immediately in the "Effective Text" tab.
 * As the user types into the input box, a "rendering" state will be displayed.
 * The "rendering" state persists for as long as the user is changing more than one key every {@code TYPING_MILLIS} milliseconds.
 * Then the text the user has typed is rendered into the webview {@code renderer}.
 * <br/>
 * This is to simulate a situation where rendering might instead be a longer process, such as an external service
 * request to query data, so we want to wait for the user to stop typing before making the service call.
 */
public class SimpleEditorDemoController {
    // Create a scheduler pool. This would be better done with a guava ThreadFactoryBuilder and/or stored separately
    // as a static shared pool.
    private static final ScheduledExecutorService SCHEDULER_POOL = Executors.newScheduledThreadPool(1,
            runnable -> {
                Thread thread = new Thread(runnable, "debounce-thread");
                thread.setDaemon(true);
                thread.setPriority(Thread.MIN_PRIORITY);
                return thread;
            });

    // The approximate milliseconds when it is determined the user has probably finished typing
    private static final int TYPING_MILLIS = 1000;

    @FXML
    private TextInputControl textEntry; // the text field the user enters text into

    @FXML
    private TextInputControl effectiveText; // the node to display the template with the user entered text inserted

    @FXML
    private WebView renderer; // the html renderer

    @FXML
    private Node loadingNode; // the node to display while loading
    private BooleanProperty rendering = new SimpleBooleanProperty(false); // controls the rendering state

    //invoke by fxml loader
    public void initialize() throws Exception {
        // Load the template to a string.
        String template = buildTemplateString();

        // Build the debouncer
        Debouncer<String> debouncer = buildTextRenderDebouncer();

        // Sets up the filled out template binding
        StringExpression formatted = Bindings.format(template, textEntry.textProperty());
        // effectiveText updates instantly while user is typing
        effectiveText.textProperty().bind(formatted);

        // add a listener so that when the filled out template changes
        // switches into rendering mode and pushes the latest up to the debouncer.
        formatted.addListener((observable, oldValue, newValue) -> {
            rendering.set(true);
            debouncer.accumulate(newValue);
        });

        // set up the control of the display through the rendering state
        loadingNode.visibleProperty().bind(rendering);
        loadingNode.managedProperty().bind(rendering);
        renderer.visibleProperty().bind(rendering.not());

        // set the renderer to show any content already entered before initialize is called.
        renderer.getEngine().loadContent(formatted.get());
    }

    private Debouncer<String> buildTextRenderDebouncer() {

        return Debouncer
                .accumulating(new LatestValueAccumulator<String>())// only care about the final state of what the user typed
                .drainingOn(Platform::runLater) // Updates to UI should be done on the FX Application thread
                .regulating(runnable -> new QuiescentRegulator(SCHEDULER_POOL, TYPING_MILLIS, runnable)) // waiting for the user to stop typing
                .draining(content -> {
                    renderer.getEngine().loadContent(content); // render the user text in the renderer
                    rendering.set(false); // set the rendering state to finished.
                });
    }

    private String buildTemplateString() throws URISyntaxException, IOException {
        // Not the most efficient way to load the text, but works for now...
        URL templateURL = SimpleEditorDemoController.class.getResource("/editor/template.html");
        Path path = Paths.get(templateURL.toURI());
        List<String> lines = Files.readAllLines(path);
        return lines.stream().collect(Collectors.joining("\n"));
    }
}

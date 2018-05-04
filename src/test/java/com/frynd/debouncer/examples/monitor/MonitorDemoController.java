package com.frynd.debouncer.examples.monitor;

import com.frynd.debouncer.Debouncer;
import com.frynd.debouncer.accumulator.impl.LatestValueAccumulator;
import com.frynd.debouncer.accumulator.impl.MapAccumulator;
import com.frynd.debouncer.drainer.Drainers;
import com.frynd.debouncer.regulator.impl.DelayedRegulator;
import com.frynd.debouncer.regulator.impl.QuiescentRegulator;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * Controller for the monitor UI.
 * <br/>
 * Allows for creation of event producers.
 * Displays events from producers on a bar chart and in a table.
 * <br/>
 * While employing the debouncer causes a small decrease in the number of event processed per second,
 * without the debouncer the ui eventually only refreshes sporadically.
 */
public class MonitorDemoController {
    //Number of producers to add at a time for the batch add operations.
    //8 is the number of colors the default fx bar chart supports before looping
    //Also the initial number of producers.
    private static final int PRODUCER_BATCH_COUNT = 8;

    //Low end and high end of the events per second producer batches
    private static final int PRODUCER_BATCH_LOW = 300;
    private static final int PRODUCER_BATCH_HIGH = 900;

    //Number of seconds between printing the number of events per second
    private static final int SECONDS_PER_PRINT = 2;

    //Executor service for debouncing and printing events per second
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(buildDebounceThreadFactory());

    //Bar chart for displaying the events on the monitor tab
    @FXML
    private BarChart<String, Integer> chart;

    //Tabular view of events on the monitor tab
    @FXML
    private TableView<LastUpdate> eventTable;

    //The name for the producer the user wants to configure
    @FXML
    private TextField name;

    //Checkbox to determine if the user wants to configure a rate-limited or cpu-bound producer
    //Selected if the user wants rate-limited
    @FXML
    private CheckBox limited;

    //Events-per-second label wired in to add the value from slider to the display
    @FXML
    private Label epsLabel;

    //Slider for how many events-per-second the user wants to configure.
    @FXML
    private Slider epsSlider;

    //Button for creating and adding the configured producer
    @FXML
    private Button addProducerButton;

    //Toggle button for using debouncer or not
    //Selected if the user wants to use the debouncer.
    @FXML
    private ToggleButton debounceToggle;

    //List of event producers on the configuration tab
    @FXML
    private ListView<EventProducer> producerList;

    //Map of chart series by id for lookup
    private Map<String, XYChart.Series<String, Integer>> seriesById = new HashMap<>();
    //Map of LastUpdate by id for lookup
    private Map<String, LastUpdate> lastUpdateById = new HashMap<>();

    //Producer id counter for generating (roughly) unique producer names
    private AtomicInteger producerIdCounter = new AtomicInteger(0);
    //Counter for the number of events
    //Reset on every print
    private AtomicInteger eventCounter = new AtomicInteger(0);

    //Debouncer for event producers
    private Debouncer<Update> producerDebouncer;

    //Callback event producers will invoke for each event
    private BiConsumer<String, Integer> callback = (id, value) -> {
        if (debounceToggle.isSelected()) {
            //using debouncer to handle events
            producerDebouncer.accumulate(new Update(id, value));
        } else {
            //otherwise, invoke putData directly
            //If putData must be run entirely on the FX application thread
            //this significantly reduces the number of events per second processed
            putData(id, value);
        }
        //Increment the number of events received
        eventCounter.incrementAndGet();
    };

    //Keeping track of if current name in the name text field is in use
    private BooleanProperty nameInUse = new SimpleBooleanProperty(false);
    //Keeping track of if currently checking if the name in the name text field is in use
    private BooleanProperty checkingName = new SimpleBooleanProperty(false);

    //invoked by fxml loader
    public void initialize() {

        //Debouncer for receiving events from event producer
        producerDebouncer = Debouncer
                //Accumulate by update name as key, only store the latest value
                .accumulating(new MapAccumulator<>(Update::getName, LatestValueAccumulator<Update>::new))
                //Setup to only run updates every 100ms.
                //500ms creates a stutter, but reduces update overhead
                .regulating(runnable -> new DelayedRegulator(scheduler, 100, runnable))
                //Drain map to update ui for each update event received
                .draining(Drainers.drainMap((name, update) -> {
                    if (update != null) {
                        putData(update.getName(), update.getValue());
                    }
                }));

        //Debouncer for checking the name is unique.
        //Technically, this isn't needed as the check is fast enough, but was a nice example
        //of a quiescent regulator
        Debouncer<String> nameDebouncer = Debouncer
                //Only need the last value typed
                .accumulating(new LatestValueAccumulator<String>())
                //Update on the FX application thread
                .drainingOn(Platform::runLater)
                //Wait 500ms after the user is finished typing to run update
                .regulating(runnable -> new QuiescentRegulator(scheduler, 500, runnable))
                //Check if the name entered is already in use
                .draining(text -> {
                    checkNameInUse();
                    checkingName.set(false);
                });

        //Schedule the printing of number of events received per second
        scheduler.scheduleAtFixedRate(() -> {
            //get and reset event counter
            int eventsPerSecond = eventCounter.getAndSet(0) / SECONDS_PER_PRINT;
            System.out.println(NumberFormat.getInstance().format(eventsPerSecond) + " events per second.");
        }, SECONDS_PER_PRINT, SECONDS_PER_PRINT, TimeUnit.SECONDS);

        //Setup the events-per-second configuration label
        epsLabel.textProperty().bind(Bindings.format("Events per second (%2.0f)", epsSlider.valueProperty()));

        //Setup the producer list to show remove buttons
        producerList.setCellFactory(view -> new EventProducerCell());

        //Setup the listener to start checking name while user is typing
        name.textProperty().addListener((observable, oldValue, newValue) -> {
            checkingName.set(true);
            nameDebouncer.accumulate(newValue);
        });

        //Bind the text of the add producer button to display why it stays disabled
        addProducerButton.textProperty().bind(
                Bindings.when(nameInUse).then("Name is in use.").otherwise("Add Event Producer")
        );

        //Disables the add producer button while checking name, if name is not unique, or if name is empty
        addProducerButton.disableProperty().bind(checkingName.or(nameInUse).or(name.textProperty().isEmpty()));

        //Add some producers to make startup interesting
        addManyLimitedProducers();
    }

    @FXML
    public void addManyLimitedProducers() {
        for (int i = 1; i < PRODUCER_BATCH_COUNT + 1; i++) {
            //spread out generated eps over interesting range
            int eps = remapRange(i, PRODUCER_BATCH_COUNT, PRODUCER_BATCH_LOW, PRODUCER_BATCH_HIGH);
            String id = "Producer-" + producerIdCounter.incrementAndGet();
            System.out.println("Creating producer [" + id + "] with " + eps + " events per second.");
            addLimitedProducer(id, eps);
        }
    }

    @FXML
    public void addManyUnlimitedProducers() {
        for (int i = 1; i < PRODUCER_BATCH_COUNT + 1; i++) {
            String id = "Producer-" + producerIdCounter.incrementAndGet();
            System.out.println("Creating producer [" + id + "] with no limit on events per second.");
            addUnlimitedProducer(id);
        }
    }

    @FXML
    public void addConfiguredProducer() {
        String producerName = name.getText();
        if (limited.isSelected()) {
            double epsValue = epsSlider.getValue();
            addLimitedProducer(producerName, (int) epsValue);
        } else {
            addUnlimitedProducer(producerName);
        }
    }

    private void addLimitedProducer(String producerName, int epsValue) {
        putData(producerName, 0);
        checkNameInUse();
        producerList.getItems().add(new EventProducer(producerName, epsValue, callback));
    }

    private void addUnlimitedProducer(String producerName) {
        putData(producerName, 0);
        checkNameInUse();
        producerList.getItems().add(new EventProducer(producerName, callback));
    }

    private void checkNameInUse() {
        nameInUse.set(seriesById.containsKey(name.getText()));
    }

    private void putData(String id, int value) {
        XYChart.Series<String, Integer> series = seriesById.computeIfAbsent(id, this::createSeriesSafe);
        LastUpdate lastUpdate = lastUpdateById.computeIfAbsent(id, this::createLastUpdateSafe);

        if (value < 0) {
            chart.getData().remove(series);
            eventTable.getItems().remove(lastUpdate);
            seriesById.remove(id);
            checkNameInUse();
        } else {
            series.getData().get(0).setYValue(value);
            lastUpdate.setLastValue(value);
        }
    }

    private LastUpdate createLastUpdateSafe(String key) {
        if (Platform.isFxApplicationThread()) {
            return createLastUpdate(key);
        }
        return CompletableFuture.supplyAsync(() -> createLastUpdate(key), Platform::runLater).join();
    }

    private XYChart.Series<String, Integer> createSeriesSafe(String key) {
        if (Platform.isFxApplicationThread()) {
            return createSeries(key);
        }
        return CompletableFuture.supplyAsync(() -> createSeries(key), Platform::runLater).join();
    }

    private LastUpdate createLastUpdate(String key) {
        LastUpdate created = new LastUpdate(key, -1);
        eventTable.getItems().add(created);
        return created;
    }

    private XYChart.Series<String, Integer> createSeries(String key) {
        XYChart.Series<String, Integer> created = new XYChart.Series<>();
        created.setName(key);
        created.getData().add(new XYChart.Data<>("Latest", 0));
        chart.getData().add(created);
        return created;
    }

    private static int remapRange(int value, int origHigh, int newLow, int newHigh) {
        int origLow = 1;
        int zeroedOldHigh = origHigh - origLow;
        int zeroedNewHigh = newHigh - newLow;
        int zeroedValue = value - origLow;

        float ratioToZeroedHigh = zeroedValue / ((float) zeroedOldHigh);
        float zeroedNewValue = ratioToZeroedHigh * zeroedNewHigh;
        float adjustedNewValue = zeroedNewValue + newLow;
        return Math.round(adjustedNewValue);
    }

    private ThreadFactory buildDebounceThreadFactory() {
        AtomicInteger count = new AtomicInteger(0);
        return runnable -> {
            Thread thread = new Thread(runnable, "delay-pool-" + count.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
    }

    private class EventProducerCell extends ListCell<EventProducer> {

        private Label label = new Label();
        private Button remove = new Button("Remove");

        private BorderPane box = new BorderPane();

        EventProducerCell() {
            remove.setOnAction(evt -> {
                getItem().shutdown();
                producerList.getItems().remove(getItem());
            });
            remove.setTooltip(new Tooltip("Remove the producer and stop publishing events."));
            box.setLeft(label);
            box.setRight(remove);
        }

        @Override
        protected void updateItem(EventProducer item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null && !empty) {
                label.setText(item.getName());
                setGraphic(box);
            } else {
                setGraphic(null);
            }
        }
    }

    private static class Update {
        private final String name;
        private final int value;

        private Update(String name, int value) {
            this.name = name;
            this.value = value;
        }

        String getName() {
            return name;
        }

        int getValue() {
            return value;
        }

    }

    //protected for access via PropertyValueFactory
    protected static class LastUpdate {
        private StringProperty name = new SimpleStringProperty(this, "name", null);

        private ObjectProperty<Integer> lastValue = new SimpleObjectProperty<>(this, "lastValue", -1);

        LastUpdate(String name, int i) {
            this.name.setValue(name);
            this.lastValue.setValue(i);
        }

        @SuppressWarnings("unused")//accessed via PropertyValueFactory
        public ReadOnlyStringProperty nameProperty() {
            return name;
        }

        @SuppressWarnings("unused")//accessed via PropertyValueFactory
        public ReadOnlyObjectProperty<Integer> lastValueProperty() {
            return lastValue;
        }

        void setLastValue(int lastValue) {
            this.lastValue.set(lastValue);
        }

    }
}

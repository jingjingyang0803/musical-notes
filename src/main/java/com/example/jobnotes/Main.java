package com.example.jobnotes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {

    private Job currentJob= new Job("test") ;  // Job instance

    private ObservableList<Job> jobs = FXCollections.observableArrayList();

    // Create ListView on left
    private ListView<Job> jobsListView;

    // Create GridPane and its components on right top
    private ScrollPane jobEditScrollPane = new ScrollPane();
    private GridPane jobEditPaneGrid = new GridPane();
    private TextField jobNameField = new TextField();
    private Label jobNameWarningLabel = new Label("Job name should be 1-20 characters long. \nPress Enter/Tab to save changes.");
    private Spinner<Integer> fromNoteSpinner = new Spinner<>(1, currentJob.getToNote() - 1, currentJob.getFromNote());
    private Spinner<Integer> toNoteSpinner = new Spinner<>(currentJob.getFromNote() + 1, 127, currentJob.getToNote());
    private ToggleGroup internalGroup = new ToggleGroup();
    private HBox internalsHbox = new HBox(10);// Spacing of 10 pixels between each radio button
    // Labels for Sliders
    private Label durationLabel = new Label("Duration: 0 ms");
    private Label decayLabel = new Label("Decay: 0 ms");
    private Label gapLabel = new Label("Gap: 0 ms");
    // Sliders
    private Slider durationSlider = new Slider(100, 5000, 1000);// note duration min 100ms, max 5000ms
    private Slider decaySlider = new Slider(100, 4500, 500);// note decay min 100ms, max 4500ms
    private Slider gapSlider = new Slider(100, 500, 100);// note gap min 100ms, max 500ms
    // Sliders
    private CheckBox checkBox = new CheckBox("Use default times");
    private Label totaltimeLabel = new Label("Total Note Time: 0 ms");
    private Canvas noteTimeCanvas = new Canvas(300, 50);

    // Velocity selection setup
    private ToggleGroup velocityGroup = new ToggleGroup();
    private RadioButton singularVelocityButton = new RadioButton("Singular Velocity");
    private RadioButton specificVelocitiesButton = new RadioButton("Specific Velocities");
    private Label specificVelocitiesWarningLabel = new Label("Please enter comma-separated numbers within 1-127. \nPress Enter/Tab to save changes.");
    private RadioButton distributedVelocitiesButton = new RadioButton("Distributed Velocities");
    // Velocity control Components
    private Slider singularVelocitySlider = new Slider(1, 127, 80);
    private Label singularVelocityLabel = new Label("80");
    private TextField specificVelocitiesField = new TextField();
    private Spinner<Integer> firstVelocitySpinner = new Spinner<>(1, 127, 40);
    private Spinner<Integer> lastVelocitySpinner = new Spinner<>(firstVelocitySpinner.getValue()+1, 127, 100);
    private Spinner<Integer> countSpinner = new Spinner<>(0, lastVelocitySpinner.getValue()-firstVelocitySpinner.getValue()+1, 4);  // range for count

    // Create TableView on right bottom
    private ObservableList<Note> noteList = FXCollections.observableArrayList();
    private TableView<Note> noteTable;

    @Override
    public void start(Stage primaryStage) {
        //
        // Customize Job List
        //
        Job job1 = new Job("Job one");
        job1.setFromNote(80);
        job1.setToNote(100);
        job1.setVelocity(85);

        Job job2 = new Job("Job two");
        job2.setNoteDuration(3500);
        List<Integer> vs = Arrays.asList(60, 60, 100);
        job2.setSpecificVelocities(vs);

        Job job3 = new Job("Job three");
        job3.setNoteGap(300);
        job3.setDistributedVelocities(50, 90, 4);

        // Create a list of jobs
        jobs.addAll(job1,job2,job3);

        //
        // Left Section
        //
        jobsListView = getJobListView();

        //
        // Right Top Section
        //
        setupJobComponents();// // GridPane configuration
        updateUIWithJob(currentJob);// Initialize UI with default job

        //
        // Right Bottom Section
        //
        noteTable = getNoteTableView();


        setupListeners(); // Add listeners for job changes, slider movements, etc.

        setupStageTopBottom(primaryStage);// Final stage setup option 1: top, right bottom, left bottom
//        setupStageLeftRight(primaryStage);// Final stage setup option 2: left, top right, bottom right
    }

   private ListView<Job> getJobListView() {
        // Create a ListView for left section using a list of Jobs
        jobsListView = new ListView<>(jobs);

        //
        // Current job
        //

        // Select the first job by default
        jobsListView.getSelectionModel().selectFirst();
        currentJob = jobs.get(0);

        jobsListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) { // Check if the new value is not null
                        int selectedIndex = jobsListView.getSelectionModel().getSelectedIndex();
                        currentJob = newValue; // Use newValue directly instead of getting it from the list
                        velocityGroup.selectToggle(null); // Reset none of the toggles are selected
                        updateUIWithJob(currentJob); // Update UI for selected job
                    }
                }
        );

        return jobsListView;
    }

    private void refreshJobListView() {
        // Trigger an update to ListView to reflect changes
        jobsListView.setItems(null);// Clear items to refresh

        jobsListView.setItems(jobs);// Set new items

        // Optionally, if jobs list is not recreated each time
        jobsListView.refresh();
    }

    private void setupIntervalToggleGroup() {
        updateIntervalToggleGroup(); // Call this method to create/update radio buttons

        TitledPane intervalTP = new TitledPane("Interval", internalsHbox);
        intervalTP.setCollapsible(true);
        jobEditPaneGrid.add(intervalTP, 0, 5, 2, 1);  // Adds the TitledPane to the grid.
    }

    public void updateIntervalToggleGroup() {
        internalsHbox.getChildren().clear(); // Clear existing radio buttons

        List<Job.Interval> intervals = Arrays.asList(Job.Interval.ONE, Job.Interval.THREE, Job.Interval.SIX, Job.Interval.TWELVE);
        for (Job.Interval interval : intervals) {
            RadioButton button = new RadioButton(interval.name());
            button.setToggleGroup(internalGroup);

            // Check if interval is the current job's interval
            if (interval == currentJob.getInterval()) {
                button.setSelected(true);
            }

            // Set an action on the radio button to update current job's interval
            button.setOnAction(event -> {
                currentJob.setInterval(interval);

                refreshJobListView();// update list of jobs whenever job details change
                refreshNotesTableView();// update table of notes whenever job details change
            });

            internalsHbox.getChildren().add(button); // Add the button to the HBox
        }
    }

    private void setupSliders() {
        // set slider properties
        durationSlider.setShowTickMarks(true);// display tick marks on the slider
        decaySlider.setShowTickMarks(true);
        gapSlider.setShowTickMarks(true);
        durationSlider.setShowTickLabels(true);// show labels for the tick marks
        decaySlider.setShowTickLabels(true);
        gapSlider.setShowTickLabels(true);
        durationSlider.setMajorTickUnit(100);// set the unit distance between major tick marks
        decaySlider.setMajorTickUnit(100);
        gapSlider.setMajorTickUnit(10);
        durationSlider.setBlockIncrement(100);// thumb move when using arrow keys
        decaySlider.setBlockIncrement(100);
        gapSlider.setBlockIncrement(10);

        VBox vbox = new VBox();
        vbox.setSpacing(10); // Sets the space to 10
        vbox.getChildren().addAll(durationLabel, durationSlider, decayLabel, decaySlider, gapLabel, gapSlider);

        TitledPane noteTimesTP = new TitledPane("Note Times", vbox);
        noteTimesTP.setCollapsible(true);
        noteTimesTP.setExpanded(false);// set this TitledPane to be collapsed by default when your application starts
        jobEditPaneGrid.add(noteTimesTP, 0, 6, 2, 1);  // Adds the TitledPane to the grid at column 0, row 5, and makes it span across 2 columns and 1 row.
    }

    private void setupCheckBoxAndCanvas() {
        //
        // CheckBox
        //
        jobEditPaneGrid.add(checkBox, 0, 7, 2, 1);  // Adds the checkBox to the grid at column 0, row 6, and makes it span across 2 columns and 1 row.

        //
        // Label to show total note time
        //
        jobEditPaneGrid.add(totaltimeLabel, 0, 8, 2, 1);  // Adds the time label to the grid at column 0, row 7, and makes it span across 2 columns and 1 row.

        //
        // Canvas
        //
        updateCanvas(); // initializes the canvas
        jobEditPaneGrid.add(noteTimeCanvas, 0, 9, 2, 1);  // Adds the canvas to the grid at column 0, row 8, and makes it span across 2 columns and 1 row.
    }

    private void setupVelocityToggleGroup() {
        GridPane velocityGrid = new GridPane();
        velocityGrid.setAlignment(Pos.TOP_CENTER);
        velocityGrid.setHgap(10);
        velocityGrid.setVgap(15);
        velocityGrid.setPadding(new Insets(25));

        singularVelocityButton.setToggleGroup(velocityGroup);
        specificVelocitiesButton.setToggleGroup(velocityGroup);
        distributedVelocitiesButton.setToggleGroup(velocityGroup);

        // Singular Velocity Components
        velocityGrid.add(singularVelocityButton, 0, 0);
        velocityGrid.add(singularVelocitySlider, 1, 0);
        velocityGrid.add(singularVelocityLabel, 1, 1);

        // Specific Velocities Components
        velocityGrid.add(specificVelocitiesButton, 0, 2);
        velocityGrid.add(specificVelocitiesField, 1, 2);
        velocityGrid.add(specificVelocitiesWarningLabel, 1, 3);
        specificVelocitiesWarningLabel.setWrapText(true);  // Enable text wrapping within the label
        specificVelocitiesWarningLabel.setMinHeight(Control.USE_PREF_SIZE);  // Set minimum height to use preferred size

        Label firstVelocityLabel = new Label("First Velocity:");
        Label lastVelocityLabel = new Label("Last Velocity:");
        Label countLabel = new Label("Count:");

        // Distributed Velocities Components
        velocityGrid.add(distributedVelocitiesButton, 0, 4,2,1);
        velocityGrid.add(firstVelocityLabel, 0, 5);
        velocityGrid.add(firstVelocitySpinner, 1, 5);
        velocityGrid.add(lastVelocityLabel, 0, 6);
        velocityGrid.add(lastVelocitySpinner, 1, 6);
        velocityGrid.add(countLabel, 0, 7);
        velocityGrid.add(countSpinner, 1, 7);


        singularVelocityLabel.setAlignment(Pos.CENTER);
        firstVelocityLabel.setAlignment(Pos.CENTER_RIGHT);
        lastVelocityLabel.setAlignment(Pos.CENTER_RIGHT);
        countLabel.setAlignment(Pos.CENTER_RIGHT);

        // set the minimum or preferred width to see the effect
        singularVelocityLabel.setMinWidth(200);
        firstVelocityLabel.setMinWidth(120);
        lastVelocityLabel.setMinWidth(120);
        countLabel.setMinWidth(120);

        TitledPane velocityTP = new TitledPane("Note Velocities", velocityGrid);
        velocityTP.setCollapsible(true);
        velocityTP.setExpanded(false);
        jobEditPaneGrid.add(velocityTP, 0, 10, 2, 1);
    }

    private void setupJobComponents() {
        jobEditPaneGrid.setAlignment(Pos.TOP_CENTER);  // Center align the GridPane
        jobEditPaneGrid.setHgap(10);  // Set horizontal gap between grid cells
        jobEditPaneGrid.setVgap(15);  // Set vertical gap between grid cells
        jobEditPaneGrid.setPadding(new Insets(25));  // Set the padding for the GridPane to 25 units on all sides
        jobEditPaneGrid.setMinSize(0, 0);  // Set minimum width and height to 0

        //
        // Text for heading
        //
        Text heading = new Text("Please customize the job details");
        heading.setFont(new Font(20)); // Set font size to 20
        jobEditPaneGrid.add(heading, 0, 0, 2, 1);  // Adds the heading to the grid at column 0, row 0, and makes it span across 2 columns and 1 row.
        GridPane.setHalignment(heading, HPos.CENTER);

        //
        // Labels
        //
        Label jobNameLabel = new Label("Job Name:");  // Label for the job name field
        Label fromNoteLabel = new Label("From Note:");  // Label for the from note spinner
        Label toNoteLabel = new Label("To Note:");  // Label for the to note spinner
        jobEditPaneGrid.add(jobNameLabel, 0, 1);
        jobEditPaneGrid.add(jobNameWarningLabel, 1, 2, 2, 1);
        jobNameWarningLabel.setWrapText(true);  // Enable text wrapping within the label
        jobNameWarningLabel.setMinHeight(Control.USE_PREF_SIZE);  // Set minimum height to use preferred size

        jobEditPaneGrid.add(fromNoteLabel, 0, 3);
        jobEditPaneGrid.add(toNoteLabel, 0, 4);

        //
        // TextField and Spinners
        //
        jobEditPaneGrid.add(jobNameField, 1, 1);
        jobEditPaneGrid.add(fromNoteSpinner, 1, 3);
        jobEditPaneGrid.add(toNoteSpinner, 1, 4);

        setupIntervalToggleGroup();// Setup RadioButtons for intervals
        setupSliders();
        setupCheckBoxAndCanvas();
        setupVelocityToggleGroup();

        jobEditScrollPane.setContent(jobEditPaneGrid);
        jobEditScrollPane.setFitToWidth(true);  // Ensures the grid expands to fill the scroll pane width
        jobEditScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);  // Vertical scroll bar
    }

    private void updateCanvas() {
        GraphicsContext gc = noteTimeCanvas.getGraphicsContext2D();

        // Calculate the total
        int total = currentJob.getNoteDuration() + currentJob.getNoteDecay() + currentJob.getNoteGap();
        totaltimeLabel.setText("Total Note Time: " + total + " ms");

        // Calculate the percentage of each attribute
        double durationPercentage = (double) currentJob.getNoteDuration() / total;
        double decayPercentage = (double) currentJob.getNoteDecay() / total;
        double gapPercentage = (double) currentJob.getNoteGap() / total;

        // Calculate the starting point for each rectangle
        double durationWidth = durationPercentage * noteTimeCanvas.getWidth();
        double decayWidth = decayPercentage * noteTimeCanvas.getWidth();
        double decayStartX = durationWidth; // The decay rectangle starts where the duration rectangle ends

        // Draw the duration rectangle with a solid color
        gc.setFill(Color.TEAL);
        gc.fillRect(0, 0, durationWidth, 50);

        // Create a LinearGradient for the decay rectangle
        Stop[] decayGradientStops = new Stop[] {
                new Stop(0, Color.TEAL), // Start color of the gradient
                new Stop(1, Color.BEIGE)  // End color of the gradient
        };

        // Create a linear gradient
        LinearGradient decayGradient = new LinearGradient(
                0, 0, 1, 0,
                true,
                CycleMethod.NO_CYCLE,
                decayGradientStops
        );

        // Draw the decay rectangle with the gradient
        gc.setFill(decayGradient);
        gc.fillRect(decayStartX, 0, decayWidth, 50);

        // Draw the gap rectangle with a solid color
        double gapStartX = decayStartX + decayWidth; // It starts where the decay rectangle ends
        double gapWidth = gapPercentage * noteTimeCanvas.getWidth();
        gc.setFill(Color.BEIGE);
        gc.fillRect(gapStartX, 0, gapWidth, 50);

        // Set the stroke color
        gc.setStroke(Color.BLACK); // This will set the color of the borders

        // Set the line width for the borders
        gc.setLineWidth(1); // This will set the border width to 1 pixel

        // Draw borders around the rectangles
        gc.strokeRect(0, 0, durationWidth, 50); // Border for duration rectangle
        gc.strokeRect(decayStartX, 0, decayWidth, 50); // Border for decay rectangle
        gc.strokeRect(gapStartX, 0, gapWidth, 50); // Border for gap rectangle

        // Set the fill color for the text
        gc.setFill(Color.BLACK);

        // Add text to the rectangles
        gc.fillText("Duration", 5, 30);
        gc.fillText("Decay", durationPercentage * noteTimeCanvas.getWidth() + 5, 30);
        gc.fillText("Gap", (durationPercentage + decayPercentage) * noteTimeCanvas.getWidth() + 5, 30);
    }
    
    private void updateUIWithJob(Job job) {
        // Update UI components based on updated job
        jobNameField.setText(job.getName());
        fromNoteSpinner.getValueFactory().setValue(job.getFromNote());
        toNoteSpinner.getValueFactory().setValue(job.getToNote());

        updateIntervalToggleGroup(); // update the radio buttons based on the current job

        durationLabel.setText("Duration: " + currentJob.getNoteDuration() + " ms");
        decayLabel.setText("Decay: " + currentJob.getNoteDecay() + " ms");
        gapLabel.setText("Gap: " + currentJob.getNoteGap() + " ms");

        durationSlider.setValue(job.getNoteDuration());
        decaySlider.setValue(job.getNoteDecay());
        gapSlider.setValue(job.getNoteGap());

        updateCanvas();
    }

    private List<Note> getNotes() {
        // Retrieve the list of note numbers from the job
        List<Integer> noteNumbers = currentJob.getNotes();

        // Retrieve the list of velocities from the job
        List<Integer> velocities = currentJob.getVelocities();

        // Initialize an ArrayList to store the Note objects
        List<Note> notes = new ArrayList<>();

        // Retrieve the duration, decay, and gap values from the job
        int noteDuration = currentJob.getNoteDuration();
        int noteDecay = currentJob.getNoteDecay();
        int noteGap = currentJob.getNoteGap();

        // Initialize the current time to 0
        int currentTime = 0;

        // Iterate over each note number
        for (Integer noteNumber : noteNumbers) {

            // Iterate over each velocity
            for (Integer velocity : velocities) {
                // Create a new Note object
                Note note = new Note();

                // Set the note number
                note.setNumber(noteNumber);

                // Set the note velocity
                note.setVelocity(velocity);

                // Set the start time of the note to the current time
                note.setStartTime(currentTime);

                // Set the end time of the note to the start time plus note duration and decay
                note.setEndTime(currentTime + noteDuration + noteDecay);

                // Add the note to the list of notes
                notes.add(note);

                // Update the current time for the next note by adding note duration, decay and gap
                currentTime += noteDuration + noteDecay + noteGap;
            }
        }
        return notes;
    }

    // Method to set right alignment for table column cells
    private void setRightAlignedTableColumn(TableColumn<Note, Integer> column) {
        column.setCellFactory(tc -> new TableCell<Note, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setAlignment(Pos.CENTER_RIGHT); // Right align the text
                }
            }
        });
    }

    private TableView<Note> getNoteTableView() {
        // Convert the ArrayList of Note objects to an ObservableList
        noteList = FXCollections.observableArrayList(getNotes());

        //
        // table
        //
        // Create a TableView object to display the notes
        noteTable = new TableView<>(noteList);

        // This TableView should have four TableColumn objects
        TableColumn<Note, Integer> column1 = new TableColumn<>("Note");
        TableColumn<Note, Integer> column2 = new TableColumn<>("Velocity");
        TableColumn<Note, Integer> column3 = new TableColumn<>("Start (ms)");
        TableColumn<Note, Integer> column4 = new TableColumn<>("End (ms)");

        // Set the cell value factory for each TableColumn
        column1.setCellValueFactory(new PropertyValueFactory<>("number"));
        column2.setCellValueFactory(new PropertyValueFactory<>("velocity"));
        column3.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        column4.setCellValueFactory(new PropertyValueFactory<>("endTime"));

        // Apply right alignment to the columns
        setRightAlignedTableColumn(column1);
        setRightAlignedTableColumn(column2);
        setRightAlignedTableColumn(column3);
        setRightAlignedTableColumn(column4);

        // Set the ObservableList of Note objects as the data source for the TableView
        noteTable.setItems(noteList);

        // Add the TableColumn objects to the TableView
        noteTable.getColumns().addAll(column1, column2, column3, column4);
        return noteTable;
    }

    private void refreshNotesTableView() {
        List<Note> notes = getNotes(); // Generate the list uses currentJob
        noteList.setAll(notes); // Replace the items in the TableView with the new list
        noteTable.refresh(); // Refresh the TableView to display the new items
    }

    private void setupListeners() {

        // Add an onAction event from the text field (which triggers when Enter is pressed)
        jobNameField.setOnAction(event -> {
            String newName = jobNameField.getText().trim();
            if (newName.length() > 0 && newName.length() <= 20) {
                currentJob.setName(newName);
                refreshJobListView();
                jobNameWarningLabel.setText("Job name has been updated.");
            } else {
                jobNameWarningLabel.setText("Invalid Job Name. It should be 1-20 characters long.");
            }
        });

        // Add a listener to the 'jobNameField' focused property (which triggers when Tab key is pressed)
        jobNameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { // focus lost
                String newName = jobNameField.getText().trim();
                if (newName.length() > 0 && newName.length() <= 20) {
                    currentJob.setName(newName);
                    refreshJobListView();
                    jobNameWarningLabel.setText("Job name has been updated.");
                } else {
                    jobNameWarningLabel.setText("Invalid Job Name. It should be 1-20 characters long.");
                }
            }
        });

        // Add a listener to the 'fromNoteSpinner' value property
        fromNoteSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            // update the minimum value of toNoteSpinner
            int newMin = newValue + 1;
            toNoteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(newMin, 127, Math.max(toNoteSpinner.getValue(), newMin)));

            // update job
            currentJob.setFromNote(newValue.intValue());
            refreshJobListView();// update list of jobs whenever job details change
            refreshNotesTableView();// update table of notes whenever job details change

        });

        // Add a listener to the 'toNoteSpinner' value property
        toNoteSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            // update the maximum value of fromNoteSpinner
            int newMax = Math.max(0, newValue - 1);
            fromNoteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, newMax, Math.min(fromNoteSpinner.getValue(), newMax)));

            // update job
            currentJob.setToNote(newValue.intValue());
            refreshJobListView();// update list of jobs whenever job details change
            refreshNotesTableView();// update table of notes whenever job details change

        });

        // Add listeners to the sliders value property
        durationSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            durationLabel.setText("Duration: " + newValue.intValue() + " ms");
            // update job
            currentJob.setNoteDuration(newValue.intValue());
            refreshJobListView();// update list of jobs whenever job details change
            refreshNotesTableView();// update table of notes whenever job details change
            updateCanvas(); // update the canvas
        });
        decaySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            decayLabel.setText("Decay: " + newValue.intValue() + " ms");
            // update job
            currentJob.setNoteDecay(newValue.intValue());
            refreshJobListView();// update list of jobs whenever job details change
            refreshNotesTableView();// update table of notes whenever job details change
            updateCanvas(); // update the canvas
        });
        gapSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            gapLabel.setText("Gap: " + newValue.intValue() + " ms");
            // update job
            currentJob.setNoteGap(newValue.intValue());
            refreshJobListView();// update list of jobs whenever job details change
            refreshNotesTableView();// update table of notes whenever job details change
            updateCanvas(); // update the canvas
        });

        // Add a listener to the checkBox value property
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            durationSlider.setDisable(newValue);
            decaySlider.setDisable(newValue);
            gapSlider.setDisable(newValue);

            if (newValue) {
                currentJob.setNoteDuration(1000);// default duration 1000ms
                currentJob.setNoteDecay(500);// default decay 500ms
                currentJob.setNoteGap(100);// default gap 100ms
            } else {
                // Use the latest values if newValue is false
                currentJob.setNoteDuration((int) durationSlider.getValue());
                currentJob.setNoteDecay((int) decaySlider.getValue());
                currentJob.setNoteGap((int) gapSlider.getValue());
            }

            refreshJobListView();// update list of jobs whenever job details change
            refreshNotesTableView();// update table of notes whenever job details change
            updateCanvas(); // update the canvas
        });

        // Add listeners to toggle group to update UI based on selection
        velocityGroup.selectedToggleProperty().addListener((observable, oldVal, newVal) -> {

            if (newVal == singularVelocityButton) {
                // If the singular velocity button is selected, apply the singular velocity settings
                int currentVelocity = (int) singularVelocitySlider.getValue();
                currentJob.setVelocity(currentVelocity);
                singularVelocityLabel.setText(String.valueOf(currentVelocity));
            } else if (newVal == specificVelocitiesButton) {
                // If the specific velocities button is selected, parse and apply specific velocities
                List<Integer> velocities = parseDistributedVelocities(specificVelocitiesField.getText());
                if (velocities != null) {
                    currentJob.setSpecificVelocities(velocities);
                }
            } else if (newVal == distributedVelocitiesButton) {
                // If the distributed velocities button is selected, calculate and apply distributed velocities
                try {
                    currentJob.setDistributedVelocities(
                            firstVelocitySpinner.getValue(),
                            lastVelocitySpinner.getValue(),
                            countSpinner.getValue()
                    );
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }

            // Always refresh UI components to reflect the current job and note settings
            refreshJobListView(); // update list of jobs whenever job details change
            refreshNotesTableView(); // update table of notes whenever job details change
        });

        // Listener for singular velocity
        singularVelocitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            //            singularVelocityButton.isSelected()==true
            if (singularVelocityButton.isSelected()) {
                currentJob.setVelocity(newVal.intValue());
                singularVelocityLabel.setText(newVal.intValue() + "");
                refreshJobListView();  // update list of jobs whenever job details change
                refreshNotesTableView();// update table of notes whenever job details change
            }
        });

        // Add an onAction event from the specific velocities field (which triggers when Enter is pressed)
        specificVelocitiesField.setOnAction(event -> {
            if (specificVelocitiesButton.isSelected()) {
                String text = specificVelocitiesField.getText().trim();
                List<Integer> velocities = parseDistributedVelocities(text);
                if (velocities != null && !velocities.isEmpty()) {
                    currentJob.setSpecificVelocities(velocities);
                    refreshJobListView();  // update list of jobs whenever job details change
                    refreshNotesTableView(); // update table of notes whenever job details change
                    specificVelocitiesWarningLabel.setText("Velocities have been updated.");
                } else {
                    specificVelocitiesWarningLabel.setText("Invalid input. Please enter comma-separated numbers within 1-127.");
                }
            }
        });

        // Listener for focus lost event on specific velocities field (which triggers when Tab key is pressed)
        specificVelocitiesField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && specificVelocitiesButton.isSelected()) { // Focus is lost and specific velocities button is selected
                String text = specificVelocitiesField.getText();
                List<Integer> velocities = parseDistributedVelocities(text);
                if (velocities != null && !velocities.isEmpty()) {
                    currentJob.setSpecificVelocities(velocities);
                    refreshJobListView();  // update list of jobs whenever job details change
                    refreshNotesTableView(); // update table of notes whenever job details change
                    specificVelocitiesWarningLabel.setText("Velocities have been updated.");
                } else {
                    specificVelocitiesWarningLabel.setText("Invalid input. Please enter comma-separated numbers within 1-127.");
                }
            }
        });

        // Listener for distributed velocities
        firstVelocitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            // update the minimum value of lastVelocitySpinner
            int newMin = newVal + 1;
            lastVelocitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(newMin, 127, Math.max(lastVelocitySpinner.getValue(), newMin)));

            // update the maximum value of countSpinner
            int newMax = Math.max(0, lastVelocitySpinner.getValue() - newVal + 1);
            countSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, newMax, Math.min(countSpinner.getValue(), newMax)));

            //            distributedVelocitiesButton.isSelected()==true
            if (distributedVelocitiesButton.isSelected()) {
                currentJob.setDistributedVelocities(
                        newVal,
                        lastVelocitySpinner.getValue(),
                        countSpinner.getValue()
                );
                refreshJobListView();  // update list of jobs whenever job details change
                refreshNotesTableView();// update table of notes whenever job details change
            }
        });

        lastVelocitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            // update the maximum value of lastVelocitySpinner
            int newMax = Math.max(0, newVal - 1);
            firstVelocitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, newMax, Math.min(firstVelocitySpinner.getValue(), newMax)));

            // update the maximum value of countSpinner
            int newCountMax = Math.max(0, newVal - firstVelocitySpinner.getValue() + 1);
            countSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, newCountMax, Math.min(countSpinner.getValue(), newCountMax)));

            //            distributedVelocitiesButton.isSelected()==true
            if (distributedVelocitiesButton.isSelected()) {
                currentJob.setDistributedVelocities(
                        firstVelocitySpinner.getValue(),
                        newVal,
                        countSpinner.getValue()
                );
                refreshJobListView();  // update list of jobs whenever job details change
                refreshNotesTableView();// update table of notes whenever job details change
            }
        });

        countSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (distributedVelocitiesButton.isSelected()) {
                currentJob.setDistributedVelocities(
                        firstVelocitySpinner.getValue(),
                        lastVelocitySpinner.getValue(),
                        newVal
                );
                refreshJobListView();  // update list of jobs whenever job details change
                refreshNotesTableView();// update table of notes whenever job details change
            }
        });
    }

    // Helper method to parse velocities from the text field
    private List<Integer> parseDistributedVelocities(String input) {
        if (input == null || input.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(Pattern.compile("\\d+").asPredicate())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private void setupStageLeftRight(Stage primaryStage) {
        //
        // Right SplitPane
        //
        // Create a SplitPane for right section
        SplitPane rightSection = new SplitPane();
        rightSection.setOrientation(Orientation.VERTICAL);
        rightSection.getItems().addAll(jobEditScrollPane, noteTable);

        //
        // Whole SplitPane
        //
        // Create a SplitPane for the whole section
        SplitPane splitPane = new SplitPane();
        // Add left section and right section to the SplitPane
        splitPane.getItems().addAll(jobsListView, rightSection);

        //
        // Scene
        //
        // Create a Scene
        Scene scene = new Scene(splitPane, 800, 500);
        // Set scene and stage
        primaryStage.setScene(scene);
        primaryStage.setTitle("JavaFX Split View");
        primaryStage.show();
    }

    private void setupStageTopBottom(Stage primaryStage) {

        //
        // Bottom SplitPane
        //
        // Create a SplitPane for the bottom section
        SplitPane bottomSection = new SplitPane();
        // Add left section and right section to the SplitPane
        bottomSection.getItems().addAll(jobEditScrollPane, noteTable);
        bottomSection.setDividerPositions(0.6);// adjust the size of each section to proper
        //
        // Whole SplitPane
        //
        // Create a SplitPane for whole section
        SplitPane topBottomSection = new SplitPane();
        topBottomSection.setOrientation(Orientation.VERTICAL);
        topBottomSection.getItems().addAll(jobsListView, bottomSection);
        topBottomSection.setDividerPositions(0.15);// adjust the size of each section to proper

        //
        // Scene
        //
        // Create a Scene
        Scene scene = new Scene(topBottomSection, 800, 700);
        // Set scene and stage
        primaryStage.setScene(scene);
        primaryStage.setTitle("JavaFX Split View");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

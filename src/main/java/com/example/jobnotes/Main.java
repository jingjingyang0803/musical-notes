package com.example.jobnotes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TitledPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
    private Job job1 = new Job("Job one");
    private Job job2 = new Job("Job two");
    private Job job3 = new Job("Job three");
    // Create ListView on left
    private ListView<Job> jobsListView;

    // Create GridPane and its components on right top
    private GridPane grid = new GridPane();
    private TextField jobNameField = new TextField();
    private Spinner<Integer> fromNoteSpinner = new Spinner<>(0, 127, 0);
    private Spinner<Integer> toNoteSpinner = new Spinner<>(0, 127, 0);
    private ToggleGroup group = new ToggleGroup();
    private HBox hbox = new HBox(10);// Spacing of 10 pixels between each radio button
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
    private Canvas canvas = new Canvas(300, 50);

    // Create TableView on right bottom
    private ObservableList<Note> noteList = FXCollections.observableArrayList();
    private TableView<Note> table;

    //TODO: rename var/methods like job editing view, ...
    //TODO: add inline comments
    @Override
    public void start(Stage primaryStage) {
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
        table = getNoteTableView();


        setupListeners(); // Add listeners for job changes, slider movements, etc.

        setupStage(primaryStage);// Final stage setup option 1: top, right bottom, left bottom
//        setupStage2(primaryStage);// Final stage setup option 2: left, top right, bottom right
    }

    private ObservableList<Job> getJoblist() {
        //
        // Customize Job List
        //
        job1.setVelocity(85);

        List<Integer> vs = Arrays.asList(60, 60, 100);
        job2.setSpecificVelocities(vs);

        job3.setDistributedVelocities(50, 90, 4);

        // Create a list of jobs
        ObservableList<Job> jobs = FXCollections.observableArrayList(job1, job2, job3);
        return jobs;
    }

    //TODO: refresh jobs list view and table view for velocity change as well
    private ListView<Job> getJobListView() {
        ObservableList<Job> jobs= getJoblist();
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
                        updateUIWithJob(currentJob); // Update UI for selected job
                    }
                }
        );

        return jobsListView;
    }

    private void refreshJobListView() {
        // Trigger an update to ListView to reflect changes
        jobsListView.setItems(null);// Clear items to refresh

        ObservableList<Job> jobs= getJoblist(); // getJobs() fetches the updated list
        jobsListView.setItems(jobs);// Set new items

        // Optionally, if jobs list is not recreated each time
        jobsListView.refresh();
    }

    private void setupIntervalToggleGroup() {
        updateIntervalToggleGroup(); // Call this method to create/update radio buttons

        TitledPane tp = new TitledPane("Interval", hbox);
        tp.setCollapsible(true);
        grid.add(tp, 0, 4, 2, 1);  // Adds the TitledPane to the grid.
    }

    public void updateIntervalToggleGroup() {
        hbox.getChildren().clear(); // Clear existing radio buttons

        List<Job.Interval> intervals = Arrays.asList(Job.Interval.ONE, Job.Interval.THREE, Job.Interval.SIX, Job.Interval.TWELVE);
        for (Job.Interval interval : intervals) {
            RadioButton button = new RadioButton(interval.name());
            button.setToggleGroup(group);

            // Check if interval is the current job's interval
            if (interval == currentJob.getInterval()) {
                button.setSelected(true);
            }

            // Set an action on the radio button to update current job's interval
            button.setOnAction(event -> {
                currentJob.setInterval(interval);

                refreshJobListView();// update list of jobs whenever job details change
                refreshNotesTable();// update table of notes whenever job details change
            });

            hbox.getChildren().add(button); // Add the button to the HBox
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

        TitledPane tp2 = new TitledPane("Note Times", vbox);
        tp2.setCollapsible(true);
        grid.add(tp2, 0, 5, 2, 1);  // Adds the TitledPane to the grid at column 0, row 5, and makes it span across 2 columns and 1 row.
    }

    private void setupCheckBoxAndCanvas() {
        //
        // CheckBox
        //
        grid.add(checkBox, 0, 6, 2, 1);  // Adds the checkBox to the grid at column 0, row 6, and makes it span across 2 columns and 1 row.

        //
        // Label to show total note time
        //
        grid.add(totaltimeLabel, 0, 7, 2, 1);  // Adds the time label to the grid at column 0, row 7, and makes it span across 2 columns and 1 row.

        //
        // Canvas
        //
        updateCanvas(); // initializes the canvas
        grid.add(canvas, 0, 8, 2, 1);  // Adds the canvas to the grid at column 0, row 8, and makes it span across 2 columns and 1 row.
    }

    //TODO: add three ways to set velocity
    private void setupJobComponents() {
        grid.setAlignment(Pos.TOP_CENTER);  // Center align the GridPane
        grid.setHgap(10);  // Set horizontal gap between grid cells
        grid.setVgap(15);  // Set vertical gap between grid cells
        grid.setPadding(new Insets(25));  // Set the padding for the GridPane to 25 units on all sides
        grid.setMinSize(0, 0);  // Set minimum width and height to 0

        //
        // Text for heading
        //
        Text heading = new Text("Please customize the job details");
        heading.setFont(new Font(20)); // Set font size to 20
        grid.add(heading, 0, 0, 2, 1);  // Adds the heading to the grid at column 0, row 0, and makes it span across 2 columns and 1 row.
        GridPane.setHalignment(heading, HPos.CENTER);

        //
        // Labels
        //
        Label jobNameLabel = new Label("Job Name:");  // Label for the job name field
        Label fromNoteLabel = new Label("From Note:");  // Label for the from note spinner
        Label toNoteLabel = new Label("To Note:");  // Label for the to note spinner
        grid.add(jobNameLabel, 0, 1);
        grid.add(fromNoteLabel, 0, 2);
        grid.add(toNoteLabel, 0, 3);

        //
        // TextField and Spinners
        //
        grid.add(jobNameField, 1, 1);
        grid.add(fromNoteSpinner, 1, 2);
        grid.add(toNoteSpinner, 1, 3);


        setupIntervalToggleGroup();// Setup RadioButtons for intervals
        setupSliders();
        setupCheckBoxAndCanvas();
    }

    // TODO: use gradient color to show decay
    private void updateCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Calculate the total
        int total = currentJob.getNoteDuration() + currentJob.getNoteDecay() + currentJob.getNoteGap();
        totaltimeLabel.setText("Total Note Time: " + total + " ms");

        // Calculate the percentage of each attribute
        double durationPercentage = (double) currentJob.getNoteDuration() / total;
        double decayPercentage = (double) currentJob.getNoteDecay() / total;
        double gapPercentage = (double) currentJob.getNoteGap() / total;

        // Calculate the starting point for each rectangle
        double durationWidth = durationPercentage * canvas.getWidth();
        double decayWidth = decayPercentage * canvas.getWidth();
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
        double gapWidth = gapPercentage * canvas.getWidth();
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
        gc.fillText("Decay", durationPercentage * canvas.getWidth() + 5, 30);
        gc.fillText("Gap", (durationPercentage + decayPercentage) * canvas.getWidth() + 5, 30);
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

    //TODO: align numbers to right
    private TableView<Note> getNoteTableView() {
        // Convert the ArrayList of Note objects to an ObservableList
        noteList = FXCollections.observableArrayList(getNotes());

        //
        // table
        //
        // Create a TableView object to display the notes
        table = new TableView<>(noteList);

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

        // Set the ObservableList of Note objects as the data source for the TableView
        table.setItems(noteList);

        // Add the TableColumn objects to the TableView
        table.getColumns().addAll(column1, column2, column3, column4);
        return table;
    }

    private void refreshNotesTable() {
        List<Note> notes = getNotes(); // Generate the list uses currentJob
        noteList.setAll(notes); // Replace the items in the TableView with the new list
        table.refresh(); // Refresh the TableView to display the new items
    }

    private void setupListeners() {
        // Add a listener to the 'jobNameField' text property
        jobNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Focus lost
            String newName = jobNameField.getText().trim();  // Trim leading and trailing whitespaces

            // Check if job name is not empty and not exceeding 20 characters
            if (newName.length() > 0 && newName.length() <= 20) {
                currentJob.setName(newName);  // Set the new job name
                refreshJobListView();// update list of jobs whenever job details change
            } else {
                System.out.println("Invalid Job Name. It should be 1-20 characters long.");  // Print error if invalid
            }
        });

        // Add a listener to the 'fromNoteSpinner' value property
        fromNoteSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            // update job
            currentJob.setFromNote(newValue.intValue());
            refreshJobListView();// update list of jobs whenever job details change
            refreshNotesTable();// update table of notes whenever job details change
        });

        // Add a listener to the 'toNoteSpinner' value property
        toNoteSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            // update job
            currentJob.setToNote(newValue.intValue());
            refreshJobListView();// update list of jobs whenever job details change
            refreshNotesTable();// update table of notes whenever job details change
        });

        // Add listeners to the sliders value property
        durationSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            durationLabel.setText("Duration: " + newValue.intValue() + " ms");
            // update job
            currentJob.setNoteDuration(newValue.intValue());
            refreshJobListView();// update list of jobs whenever job details change
            refreshNotesTable();// update table of notes whenever job details change
            updateCanvas(); // update the canvas
        });
        decaySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            decayLabel.setText("Decay: " + newValue.intValue() + " ms");
            // update job
            currentJob.setNoteDecay(newValue.intValue());
            refreshJobListView();// update list of jobs whenever job details change
            refreshNotesTable();// update table of notes whenever job details change
            updateCanvas(); // update the canvas
        });
        gapSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            gapLabel.setText("Gap: " + newValue.intValue() + " ms");
            // update job
            currentJob.setNoteGap(newValue.intValue());
            refreshJobListView();// update list of jobs whenever job details change
            refreshNotesTable();// update table of notes whenever job details change
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
            refreshNotesTable();// update table of notes whenever job details change
            updateCanvas(); // update the canvas
        });
    }

    private void setupStage2(Stage primaryStage) {
        //
        // Right SplitPane
        //
        // Create a SplitPane for right section
        SplitPane rightSection = new SplitPane();
        rightSection.setOrientation(Orientation.VERTICAL);
        rightSection.getItems().addAll(grid, table);

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

    private void setupStage(Stage primaryStage) {

        //
        // Bottom SplitPane
        //
        // Create a SplitPane for the bottom section
        SplitPane bottomSection = new SplitPane();
        // Add left section and right section to the SplitPane
        bottomSection.getItems().addAll(grid, table);
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
        Scene scene = new Scene(topBottomSection, 800, 850);
        // Set scene and stage
        primaryStage.setScene(scene);
        primaryStage.setTitle("JavaFX Split View");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

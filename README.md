# musical-notes

This project is a JavaFX application designed for managing musical jobs with a high level of note customization including properties such as duration, decay, and velocity. It offers a user-friendly graphical interface for visual representation and modification of musical pieces or segments.

## Features

- **Job Management:** Allows management of various musical jobs (creation not added yet).
- **Customizable Note Properties:** Enables adjustment of properties such as job range, not range, note duration, decay times, gaps between notes, and velocity.
- **Visual Feedback:** Utilizes Canvas and a detailed table to visually represent and list the temporal distribution of note times.
- **Dynamic Configuration:** Offers a responsive interface with sliders, spinners, and toggle buttons for modifying settings.

## Getting Started

Follow these instructions to get a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

- Java 17 or higher
- JavaFX SDK 17 or higher
- Maven

  - **Set Up JavaFX**: Ensure you have the JavaFX SDK downloaded and set up on your machine. Download it from OpenJFX.
  - **Configure JavaFX Path**: Set the **`PATH_TO_FX`** environment variable to the lib directory of your JavaFX SDK. For example:

    ```bash
    export PATH_TO_FX="/path/to/javafx-sdk/lib"
    ```

## Installation

### **Option 1: Using the Command Line**

### **Step 1: Compile the Application**

Navigate to your project's root directory, then compile the source files using the following commands:

```bash
cd musical-notes/src
javac --module-path $PATH_TO_FX --add-modules javafx.controls -d main/java main/java/com/example/jobnotes/*.java
```

### **Step 2: Run the Application**

After compilation, run your application with:

```bash
java --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml -cp main/java com.example.jobnotes.Main
```

### **Option 2: Using IntelliJ IDEA**

### **Step 1: Import the Project**

1. Open IntelliJ IDEA.
2. Choose **File > Open** and select the project directory.
3. IntelliJ IDEA will automatically detect the Maven configuration and set up everything for you.

### **Step 2: Run the Application**

1. Right-click on the **`Main.java`** file in the project explorer.
2. Select **Run 'Main.main()'** to start the application.

### **Option 3: Using Maven to Build and Run**

### **Step 1: Build the Project**

Run the following command in your project directory to build the project:

```bash
mvn clean install
```

- **Ensure Maven is Installed**: Before building the project, check if Maven is installed on your system by running **`mvn -v`** in your terminal. If Maven is not installed, download and install it from the [Apache Maven official site](https://maven.apache.org/download.cgi).

### **Step 2: Run the JAR**

After building the project, a JAR file will be generated in the **`target`** directory. Run the application using:

```bash
java --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml -jar target/musical-notes-1.0-SNAPSHOT.jar
```

## **Usage**

After starting the application, you can select a job from the left pane, customize its properties in the right pane, and see the changes reflected immediately on the visual canvas and the detailed table view for notes.

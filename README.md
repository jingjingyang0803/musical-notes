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
- Maven
- JavaFX

### Installation

Follow these steps to install and run the application:

1. **Clone the Repository**

   - Open your terminal.
   - Clone the repository by running:

     ```bash
     git clone https://github.com/jingjingyang0803/musical-notes.git
     ```

   - Navigate to the project folder:

     ```bash
     cd musical-job-notes
     ```

2. **Build the Project**

   - **Ensure Maven is Installed**: Before building the project, check if Maven is installed on your system by running **`mvn -v`** in your terminal. If Maven is not installed, download and install it from the [Apache Maven official site](https://maven.apache.org/download.cgi).
   - Build the project using Maven by executing:

     ```bash
     mvn clean install
     ```

3. **Run the Application**

   - **Set Up JavaFX**: Ensure you have the JavaFX SDK downloaded and set up on your machine. Download it from OpenJFX.
   - **Configure JavaFX Path**: Set the **`PATH_TO_FX`** environment variable to the lib directory of your JavaFX SDK. For example:

     ```bash
     export PATH_TO_FX="/path/to/javafx-sdk/lib"
     ```

   - Navigate to the **`target`** directory where the JAR file is located:

     ```bash
     cd target
     ```

   - Run the application using the following command:

     ```bash
     java --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml -jar musical-job-notes-1.0-SNAPSHOT.jar
     ```

## **Usage**

After starting the application, you can select a job from the left pane, customize its properties in the right pane, and see the changes reflected immediately on the visual canvas and the detailed table view for notes.

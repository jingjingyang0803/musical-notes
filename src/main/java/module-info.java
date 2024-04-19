module com.example.jobnotes {
	requires javafx.controls;
	requires javafx.fxml;


	opens com.example.jobnotes to javafx.fxml;
	exports com.example.jobnotes;
}
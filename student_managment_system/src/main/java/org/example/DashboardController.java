package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Locale;

public class DashboardController {
	@FXML
	private TableView<Student> studentsTable;

	@FXML
	private TextField searchField;

	@FXML
	private Label welcomeLabel;

	@FXML
	private Label totalCountLabel;

	@FXML
	private Label enrolledCountLabel;

	@FXML
	private Label graduatedCountLabel;

	@FXML
	private Label statusLabel;

	private final ObservableList<Student> students = FXCollections.observableArrayList();
	private StudentRepository repository;

	@FXML
	private void initialize() {
		configureTable();
		FilteredList<Student> filteredStudents = new FilteredList<>(students, student -> true);
		studentsTable.setItems(filteredStudents);
		searchField.textProperty().addListener((observable, oldValue, newValue) -> {
			String query = newValue == null ? "" : newValue.trim().toLowerCase(Locale.ROOT);
			filteredStudents.setPredicate(student -> query.isEmpty()
					|| student.getStudentNumber().toLowerCase(Locale.ROOT).contains(query)
					|| student.getFullName().toLowerCase(Locale.ROOT).contains(query)
					|| student.getEmail().toLowerCase(Locale.ROOT).contains(query)
					|| student.getCourse().toLowerCase(Locale.ROOT).contains(query)
					|| student.getStatus().toLowerCase(Locale.ROOT).contains(query));
		});

		try {
			repository = new StudentRepository();
			refreshStudents();
		} catch (SQLException | IOException exception) {
			showStatus("Could not open the local student database: " + exception.getMessage(), false);
		}
	}

	public void setCurrentUser(String username) {
		welcomeLabel.setText("Welcome back, " + username);
	}

	private void configureTable() {
		addColumn("Student ID", "studentNumber", 110);
		addColumn("Full name", "fullName", 170);
		addColumn("Email", "email", 210);
		addColumn("Phone", "phone", 120);
		addColumn("Course", "course", 160);
		addColumn("Status", "status", 110);
		studentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	}

	private void addColumn(String title, String property, double minimumWidth) {
		TableColumn<Student, String> column = new TableColumn<>(title);
		column.setCellValueFactory(new PropertyValueFactory<>(property));
		column.setMinWidth(minimumWidth);
		studentsTable.getColumns().add(column);
	}

	@FXML
	private void addStudent() {
		showStudentDialog(null);
	}

	@FXML
	private void editStudent() {
		Student selected = studentsTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showStatus("Select a student in the table to edit.", false);
			return;
		}
		showStudentDialog(selected);
	}

	@FXML
	private void deleteStudent() {
		Student selected = studentsTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showStatus("Select a student in the table to delete.", false);
			return;
		}

		Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
		confirmation.setTitle("Delete student");
		confirmation.setHeaderText("Delete " + selected.getFullName() + "?");
		confirmation.setContentText("This will permanently remove the student record.");
		if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
			return;
		}

		try {
			repository.delete(selected.getId());
			refreshStudents();
			showStatus("Student record deleted.", true);
		} catch (SQLException exception) {
			showStatus("Could not delete the student: " + exception.getMessage(), false);
		}
	}

	@FXML
	private void refresh() {
		refreshStudents();
		showStatus("Student list refreshed.", true);
	}

	@FXML
	private void exportCsv() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Export student list");
		chooser.setInitialFileName("students.csv");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
		File file = chooser.showSaveDialog(studentsTable.getScene().getWindow());
		if (file == null) {
			return;
		}

		StringBuilder csv = new StringBuilder("Student ID,Full name,Email,Phone,Course,Status\n");
		for (Student student : studentsTable.getItems()) {
			csv.append(csvValue(student.getStudentNumber())).append(',')
					.append(csvValue(student.getFullName())).append(',')
					.append(csvValue(student.getEmail())).append(',')
					.append(csvValue(student.getPhone())).append(',')
					.append(csvValue(student.getCourse())).append(',')
					.append(csvValue(student.getStatus())).append('\n');
		}
		try {
			Files.write(file.toPath(), csv.toString().getBytes(StandardCharsets.UTF_8));
			showStatus("Exported " + studentsTable.getItems().size() + " student records.", true);
		} catch (IOException exception) {
			showStatus("Could not export the CSV file: " + exception.getMessage(), false);
		}
	}

	@FXML
	private void logout() {
		try {
			Parent login = FXMLLoader.load(Main.class.getResource("student-view.fxml"));
			Stage stage = (Stage) studentsTable.getScene().getWindow();
			stage.setTitle("Student Management System | Login");
			stage.setMinWidth(560);
			stage.setMinHeight(380);
			stage.setScene(new Scene(login, 600, 400));
		} catch (IOException exception) {
			showStatus("Could not return to the login page.", false);
		}
	}

	private void showStudentDialog(Student existing) {
		Dialog<Student> dialog = new Dialog<>();
		dialog.setTitle(existing == null ? "Add student" : "Edit student");
		dialog.setHeaderText(existing == null ? "Enter the student's details" : "Update the student's details");
		ButtonType saveType = new ButtonType(existing == null ? "Add student" : "Save changes", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

		TextField numberField = new TextField(existing == null ? "" : existing.getStudentNumber());
		TextField nameField = new TextField(existing == null ? "" : existing.getFullName());
		TextField emailField = new TextField(existing == null ? "" : existing.getEmail());
		TextField phoneField = new TextField(existing == null ? "" : existing.getPhone());
		TextField courseField = new TextField(existing == null ? "" : existing.getCourse());
		ComboBox<String> statusChoice = new ComboBox<>(FXCollections.observableArrayList("Enrolled", "Graduated", "On leave"));
		statusChoice.setValue(existing == null ? "Enrolled" : existing.getStatus());
		Label validationLabel = new Label("All fields are required. Use a valid email address.");
		validationLabel.getStyleClass().add("dialog-hint");

		GridPane form = new GridPane();
		form.setHgap(14);
		form.setVgap(12);
		form.addRow(0, new Label("Student ID"), numberField);
		form.addRow(1, new Label("Full name"), nameField);
		form.addRow(2, new Label("Email"), emailField);
		form.addRow(3, new Label("Phone"), phoneField);
		form.addRow(4, new Label("Course"), courseField);
		form.addRow(5, new Label("Status"), statusChoice);
		form.add(validationLabel, 0, 6, 2, 1);
		form.setPrefWidth(440);
		dialog.getDialogPane().setContent(form);
		dialog.getDialogPane().getStylesheets().add(Main.class.getResource("dashboard.css").toExternalForm());
		for (TextField field : new TextField[]{numberField, nameField, emailField, phoneField, courseField}) {
			field.setPrefWidth(290);
			field.setPromptText("Required");
		}
		nameField.setPromptText("First and last name");
		emailField.setPromptText("name@example.com");
		statusChoice.setMaxWidth(Double.MAX_VALUE);

		Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveType);
		saveButton.addEventFilter(ActionEvent.ACTION, event -> {
			if (!validForm(numberField, nameField, emailField, phoneField, courseField)) {
				validationLabel.setText("Complete every field and enter a valid email address.");
				validationLabel.getStyleClass().add("error-text");
				event.consume();
			}
		});

		dialog.setResultConverter(button -> {
			if (button != saveType) {
				return null;
			}
			return new Student(existing == null ? 0 : existing.getId(), numberField.getText().trim(),
					nameField.getText().trim(), emailField.getText().trim(), phoneField.getText().trim(),
					courseField.getText().trim(), statusChoice.getValue());
		});

		dialog.showAndWait().ifPresent(student -> {
			try {
				if (existing == null) {
					repository.insert(student);
					showStatus("Student added successfully.", true);
				} else {
					repository.update(student);
					showStatus("Student details updated.", true);
				}
				refreshStudents();
			} catch (SQLException exception) {
				String message = exception.getMessage() != null && exception.getMessage().contains("students.student_number")
						? "That student ID is already in use. Choose a different ID."
						: "Could not save the student: " + exception.getMessage();
				showStatus(message, false);
			}
		});
	}

	private boolean validForm(TextField number, TextField name, TextField email, TextField phone, TextField course) {
		return !number.getText().trim().isEmpty()
				&& !name.getText().trim().isEmpty()
				&& email.getText().trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
				&& !phone.getText().trim().isEmpty()
				&& !course.getText().trim().isEmpty();
	}

	private void refreshStudents() {
		if (repository == null) {
			return;
		}
		try {
			students.setAll(repository.findAll());
			long enrolled = students.stream().filter(student -> "Enrolled".equals(student.getStatus())).count();
			long graduated = students.stream().filter(student -> "Graduated".equals(student.getStatus())).count();
			totalCountLabel.setText(Integer.toString(students.size()));
			enrolledCountLabel.setText(Long.toString(enrolled));
			graduatedCountLabel.setText(Long.toString(graduated));
		} catch (SQLException exception) {
			showStatus("Could not load student records: " + exception.getMessage(), false);
		}
	}

	private String csvValue(String value) {
		return "\"" + value.replace("\"", "\"\"") + "\"";
	}

	private void showStatus(String message, boolean success) {
		statusLabel.setText(message);
		statusLabel.getStyleClass().removeAll("success-text", "error-text");
		statusLabel.getStyleClass().add(success ? "success-text" : "error-text");
	}
}
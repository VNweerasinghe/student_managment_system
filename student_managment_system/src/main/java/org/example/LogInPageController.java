package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

public class LogInPageController {
	private static final Map<String, String> DEMO_CREDENTIALS = Map.of(
			"admin", "admin123",
			"student", "student123",
			"teacher", "teacher123"
	);

	@FXML
	private TextField usernameField;

	@FXML
	private PasswordField passwordField;

	@FXML
	private Label statusLabel;

	@FXML
	public void handleLogin(ActionEvent event) {
		String username = usernameField.getText().trim();
		String password = passwordField.getText();

		if (username.isEmpty() || password.isEmpty()) {
			showStatus("Enter both username and password.", false);
			return;
		}

		if (password.equals(DEMO_CREDENTIALS.get(username))) {
			openDashboard(username);
		} else {
			showStatus("Username or password is incorrect.", false);
		}
	}

	private void openDashboard(String username) {
		try {
			FXMLLoader loader = new FXMLLoader(Main.class.getResource("dashboard-view.fxml"));
			Parent root = loader.load();
			DashboardController controller = loader.getController();
			controller.setCurrentUser(username);

			Stage stage = (Stage) usernameField.getScene().getWindow();
			stage.setTitle("Student Management System");
			stage.setMinWidth(960);
			stage.setMinHeight(640);
			stage.setScene(new Scene(root, 1180, 760));
		} catch (IOException exception) {
			showStatus("Could not open the dashboard. Please restart the application.", false);
			exception.printStackTrace();
		}
	}

	private void showStatus(String message, boolean success) {
		statusLabel.setText(message);
		statusLabel.setStyle(success
				? "-fx-font-size: 12px; -fx-text-fill: #16804a;"
				: "-fx-font-size: 12px; -fx-text-fill: #bd3434;");
	}
}

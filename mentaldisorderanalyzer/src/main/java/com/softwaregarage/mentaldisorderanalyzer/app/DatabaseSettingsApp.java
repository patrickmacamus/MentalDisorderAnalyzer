package com.softwaregarage.mentaldisorderanalyzer.app;

import java.util.Map;

import com.softwaregarage.mentaldisorderanalyzer.util.ConfigReaderAndWriter;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class DatabaseSettingsApp extends Application {

	private Stage databaseSettingsStage;
	private AnchorPane rootPane;
	private Label mysqlImageLabel;
	private TextField urlConnectionTextField;
	private TextField databaseNameTextField;
	private TextField usernameTextField;
	private PasswordField passwordPasswordField;
	private Button saveButton;
	private Button cancelButton;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		databaseSettingsStage = primaryStage;
		databaseSettingsStage.setTitle("Database Settings");
		databaseSettingsStage.setResizable(false);
		
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(DatabaseSettingsApp.class.getResource("../ui/DatabaseSettings.fxml"));
		
		rootPane = (AnchorPane) loader.load();
		Scene scene = new Scene(rootPane);
		
		// Initialize the configuration file...
		ConfigReaderAndWriter configReadAndWrite = new ConfigReaderAndWriter();
		
		// Load the values from the properties file...
		Map<String, String> loadProperties = configReadAndWrite.readConfiguration();
		
		mysqlImageLabel = (Label) loader.getNamespace().get("mysqlImageLabel");
		mysqlImageLabel.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/icons/mysql_32.png"))));
		
		urlConnectionTextField = (TextField) loader.getNamespace().get("urlConnectionTextField");
		urlConnectionTextField.setText(loadProperties.get("mysql.urlConnection"));
		
		databaseNameTextField = (TextField) loader.getNamespace().get("databaseNameTextField");
		databaseNameTextField.setText(loadProperties.get("mysql.databaseName"));
		
		usernameTextField = (TextField) loader.getNamespace().get("usernameTextField");
		usernameTextField.setText(loadProperties.get("mysql.username"));
		
		passwordPasswordField = (PasswordField) loader.getNamespace().get("passwordPasswordField");
		passwordPasswordField.setText(loadProperties.get("mysql.password"));
		
		saveButton = (Button) loader.getNamespace().get("saveButton");
		cancelButton = (Button) loader.getNamespace().get("cancelButton");
		
		saveButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Alert alert = null;
				
				Map<String, String> writeProperties = loadProperties;
				
				writeProperties.replace("mysql.urlConnection", loadProperties.get("mysql.urlConnection"), urlConnectionTextField.getText());
				writeProperties.replace("mysql.databaseName", loadProperties.get("mysql.databaseName"), databaseNameTextField.getText());
				writeProperties.replace("mysql.username", loadProperties.get("mysql.username"), usernameTextField.getText());
				writeProperties.replace("mysql.password", loadProperties.get("mysql.password"), passwordPasswordField.getText());
				
				configReadAndWrite.writeConfiguration(writeProperties);
				
				if (!configReadAndWrite.getMessageLogs().equals("")) {
					alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error Saving Configuration.");
					alert.setHeaderText(null);
					alert.setContentText(configReadAndWrite.getMessageLogs());

					alert.showAndWait();
				} else {
					alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Saving Configuration.");
					alert.setHeaderText(null);
					alert.setContentText("You have successfully saved your database configuration.");

					alert.showAndWait();
				}
			}
		});
		
		cancelButton.setOnAction(new EventHandler<ActionEvent> () {
			@Override
			public void handle(ActionEvent event) {
				databaseSettingsStage.close();
			}
		});
		
		databaseSettingsStage.setScene(scene);
		databaseSettingsStage.show();
	}

	public Stage getStage() {
		return databaseSettingsStage = new Stage();
	}
	
}

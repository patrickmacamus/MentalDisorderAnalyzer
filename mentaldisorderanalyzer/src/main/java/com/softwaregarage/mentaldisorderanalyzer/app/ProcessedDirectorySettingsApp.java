package com.softwaregarage.mentaldisorderanalyzer.app;

import java.io.File;
import java.util.Map;

import com.softwaregarage.mentaldisorderanalyzer.util.ConfigReaderAndWriter;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class ProcessedDirectorySettingsApp extends Application {

	private Stage processedDirectorySettingsStage;
	private AnchorPane rootPane;
	private TextField scrapedProfileTextField;
	private TextField stopWordsTextField;
	private TextField slangWordTextField;
	private TextField noiseSpellTextField;
	private Button scrapedProfileButton;
	private Button stopWordsButton;
	private Button slangWordButton;
	private Button noiseSpellButton;
	private Button saveButton;
	private Button cancelButton;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		processedDirectorySettingsStage = primaryStage;
		processedDirectorySettingsStage.setTitle("Processed Directory Settings");
		processedDirectorySettingsStage.setResizable(false);
		
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(DatabaseSettingsApp.class.getResource("../ui/ProcessedDirectorySettings.fxml"));
		
		rootPane = (AnchorPane) loader.load();
		Scene scene = new Scene(rootPane);
		
		// Initialize the configuration file...
		ConfigReaderAndWriter configReadAndWrite = new ConfigReaderAndWriter();
		
		// Load the values from the properties file...
		Map<String, String> loadProperties = configReadAndWrite.readConfiguration();
		
		scrapedProfileTextField = (TextField) loader.getNamespace().get("scrapedProfileTextField");
		scrapedProfileTextField.setText(loadProperties.get("directory.scrapedFile"));
		
		stopWordsTextField = (TextField) loader.getNamespace().get("stopWordsTextField");
		stopWordsTextField.setText(loadProperties.get("directory.stopWordsFile"));
		
		slangWordTextField = (TextField) loader.getNamespace().get("slangWordTextField");
		slangWordTextField.setText(loadProperties.get("directory.slangWordFile"));
		
		noiseSpellTextField = (TextField) loader.getNamespace().get("noiseSpellTextField");
		noiseSpellTextField.setText(loadProperties.get("directory.noiseSpellFile"));
		
		scrapedProfileButton = (Button) loader.getNamespace().get("scrapedProfileButton");
		scrapedProfileButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				scrapedProfileTextField.setText(getPathDirectoryFromFileDialog(processedDirectorySettingsStage));
			}
		});
		
		stopWordsButton = (Button) loader.getNamespace().get("stopWordsButton");
		stopWordsButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				stopWordsTextField.setText(getPathDirectoryFromFileDialog(processedDirectorySettingsStage));
			}
		});
		
		slangWordButton = (Button) loader.getNamespace().get("slangWordButton");
		slangWordButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				slangWordTextField.setText(getPathDirectoryFromFileDialog(processedDirectorySettingsStage));
			}
		});
		
		noiseSpellButton = (Button) loader.getNamespace().get("noiseSpellButton");
		noiseSpellButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				noiseSpellTextField.setText(getPathDirectoryFromFileDialog(processedDirectorySettingsStage));
			}
		});
		
		saveButton = (Button) loader.getNamespace().get("saveButton");
		saveButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Alert alert = null;
				
				Map<String, String> writeProperties = loadProperties;
				
				writeProperties.replace("directory.scrapedFile", loadProperties.get("directory.scrapedFile"), scrapedProfileTextField.getText());
				writeProperties.replace("directory.stopWordsFile", loadProperties.get("directory.stopWordsFile"), stopWordsTextField.getText());
				writeProperties.replace("directory.slangWordFile", loadProperties.get("directory.slangWordFile"), slangWordTextField.getText());
				writeProperties.replace("directory.noiseSpellFile", loadProperties.get("directory.noiseSpellFile"), noiseSpellTextField.getText());
				
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
					alert.setContentText("You have successfully saved your file directory configuration.");

					alert.showAndWait();
				}
			}
		});
		
		cancelButton = (Button) loader.getNamespace().get("cancelButton");
		cancelButton.setOnAction(new EventHandler<ActionEvent> () {
			@Override
			public void handle(ActionEvent event) {
				processedDirectorySettingsStage.close();
			}
		});
		
		processedDirectorySettingsStage.setScene(scene);
		processedDirectorySettingsStage.show();
	}
	
	public Stage getStage() {
		return processedDirectorySettingsStage = new Stage();
	}

	/**
	 * This method will open the directory dialog and returns the full path directory
	 * which will be displayed in the text field and saved in the configuration
	 * file.
	 * 
	 * @param stage 
	 * @return The full path directory.
	 */
	private String getPathDirectoryFromFileDialog(Stage stage) {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Select a directory...");
		
		File pathDirectory = directoryChooser.showDialog(stage);
		return pathDirectory.getAbsolutePath();
	}
	
}

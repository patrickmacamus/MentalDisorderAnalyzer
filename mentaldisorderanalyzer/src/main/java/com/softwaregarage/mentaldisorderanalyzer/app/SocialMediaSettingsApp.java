package com.softwaregarage.mentaldisorderanalyzer.app;

import java.util.Map;

import com.softwaregarage.mentaldisorderanalyzer.util.ConfigReaderAndWriter;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class SocialMediaSettingsApp extends Application {

	private Stage socialMediaSettingsStage;
	private AnchorPane rootPane;
	private Tab facebookTab;
	private Tab twitterTab;
	private TextField facebookURLAddressTextField;
	private TextField facebookEmailIdTextField;
	private TextField twitterURLAddressTextField;
	private TextField twitterEmailIdTextField;
	private PasswordField facebookPasswordField;
	private PasswordField twitterPasswordField;
	private Button saveButton;
	private Button cancelButton;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		socialMediaSettingsStage = primaryStage;
		socialMediaSettingsStage.setTitle("Social Media Settings");
		socialMediaSettingsStage.setResizable(false);
		
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(DatabaseSettingsApp.class.getResource("../ui/SocialMediaSettings.fxml"));
		
		rootPane = (AnchorPane) loader.load();
		Scene scene = new Scene(rootPane);
		
		// Initialize the configuration file...
		ConfigReaderAndWriter configReadAndWrite = new ConfigReaderAndWriter();
		
		// Load the values from the properties file...
		Map<String, String> loadProperties = configReadAndWrite.readConfiguration();
		
		facebookTab = (Tab) loader.getNamespace().get("facebookTab");
		facebookTab.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/icons/facebook_16.png"))));
		
		twitterTab = (Tab) loader.getNamespace().get("twitterTab");
		twitterTab.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/icons/twitter_16.png"))));
		
		facebookURLAddressTextField = (TextField) loader.getNamespace().get("facebookURLAddressTextField");
		facebookURLAddressTextField.setText(loadProperties.get("facebook.urlAddress"));
		
		facebookEmailIdTextField = (TextField) loader.getNamespace().get("facebookEmailIdTextField");
		facebookEmailIdTextField.setText(loadProperties.get("facebook.emailId"));
		
		twitterURLAddressTextField = (TextField) loader.getNamespace().get("twitterURLAddressTextField");
		twitterURLAddressTextField.setText(loadProperties.get("twitter.urlAddress"));
		
		twitterEmailIdTextField = (TextField) loader.getNamespace().get("twitterEmailIdTextField");
		twitterEmailIdTextField.setText(loadProperties.get("twitter.emailId"));
		
		facebookPasswordField = (PasswordField) loader.getNamespace().get("facebookPasswordField");
		facebookPasswordField.setText(loadProperties.get("facebook.password"));
		
		twitterPasswordField = (PasswordField) loader.getNamespace().get("twitterPasswordField");
		twitterPasswordField.setText(loadProperties.get("twitter.password"));
		
		saveButton = (Button) loader.getNamespace().get("saveButton");
		saveButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Alert alert = null;
				
				Map<String, String> writeProperties = loadProperties;
				
				writeProperties.replace("facebook.urlAddress", loadProperties.get("facebook.urlAddress"), facebookURLAddressTextField.getText());
				writeProperties.replace("facebook.emailId", loadProperties.get("facebook.emailId"), facebookEmailIdTextField.getText());
				writeProperties.replace("facebook.password", loadProperties.get("facebook.password"), facebookPasswordField.getText());
				writeProperties.replace("twitter.urlAddress", loadProperties.get("twitter.urlAddress"), twitterURLAddressTextField.getText());
				writeProperties.replace("twitter.emailId", loadProperties.get("twitter.emailId"), twitterEmailIdTextField.getText());
				writeProperties.replace("twitter.password", loadProperties.get("twitter.password"), twitterPasswordField.getText());
				
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
		
		cancelButton = (Button) loader.getNamespace().get("cancelButton");
		cancelButton.setOnAction(new EventHandler<ActionEvent> () {
			@Override
			public void handle(ActionEvent event) {
				socialMediaSettingsStage.close();
			}
		});
		
		socialMediaSettingsStage.setScene(scene);
		socialMediaSettingsStage.show();
	}
	
	public Stage getStage() {
		return socialMediaSettingsStage = new Stage();
	}

}

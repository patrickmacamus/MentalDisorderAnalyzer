package com.softwaregarage.mentaldisorderanalyzer.app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.softwaregarage.mentaldisorderanalyzer.preprocessor.SVMProcessor;
import com.softwaregarage.mentaldisorderanalyzer.preprocessor.WordPreProcessor;
import com.softwaregarage.mentaldisorderanalyzer.scraper.ProfileScraper;
import com.softwaregarage.mentaldisorderanalyzer.util.ConfigReaderAndWriter;
import com.softwaregarage.mentaldisorderanalyzer.util.InternetConnector;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import weka.classifiers.evaluation.Evaluation;

public class MainWindowApp extends Application {

	private Stage mainWindowStage;
	private AnchorPane rootPane;
	private TabPane resultTabbedPane;
	private TextField profileIdTextField;
	private TextArea messageLogsTextArea;
	private ComboBox<String> yearComboBox;
	private Button facebookButton;
	private Button twitterButton;
	private Button databaseButton;
	private Button webButton;
	private Button directoryButton;
	private Button displayOutputButton;
	
	private ConfigReaderAndWriter configReadAndWrite;
	
	@SuppressWarnings("unchecked")
	@Override
	public void start(Stage primaryStage) throws Exception {
		mainWindowStage = primaryStage;
		mainWindowStage.setTitle("Mental Disorder Analyzer version 1.0");
		
		FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainWindowApp.class.getResource("../ui/MainWindow.fxml"));
        
        rootPane = (AnchorPane) loader.load();
        Scene scene = new Scene(rootPane);
        
        // START - This will load all the properties from the properties file...
        configReadAndWrite = new ConfigReaderAndWriter();
        Map<String, String> loadProperties = configReadAndWrite.readConfiguration();
        // END
        
        // START - This will set the years in the combo box...
        List<String> years = new ArrayList<String>();
        
        for (int year=2004; year<Calendar.getInstance().get(Calendar.YEAR); year++) {
        	years.add(String.valueOf(year));
        }
        
        Collections.sort(years, Collections.reverseOrder());
        
        ObservableList<String> listOfYears = FXCollections.observableArrayList(years);
        // END
        
        yearComboBox = (ComboBox<String>) loader.getNamespace().get("yearComboBox");
        yearComboBox.setItems(listOfYears);
        
        profileIdTextField = (TextField) loader.getNamespace().get("profileIdTextField");
        
        facebookButton = (Button) loader.getNamespace().get("facebookButton");
        facebookButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/icons/facebook_24.png"))));
        
        twitterButton = (Button) loader.getNamespace().get("twitterButton");
        twitterButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/icons/twitter_24.png"))));
        
        databaseButton = (Button) loader.getNamespace().get("databaseButton");
        databaseButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/icons/database_24.png"))));
        
        webButton = (Button) loader.getNamespace().get("webButton");
        webButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/icons/world-wide-web_24.png"))));
        
        directoryButton = (Button) loader.getNamespace().get("directoryButton");
        directoryButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/icons/folder-information_24.png"))));
        
        displayOutputButton = (Button) loader.getNamespace().get("displayOutputButton");
        
        resultTabbedPane = (TabPane) loader.getNamespace().get("resultTabbedPane");
        
        // Create an InternetConnector object...
        InternetConnector internetConnector = new InternetConnector();
        
        // Call the messageLogsTextArea in our MainWindow.fxml file...
        messageLogsTextArea = (TextArea) loader.getNamespace().get("messageLogsTextArea");
        
        if (internetConnector.isInternetConnected()) {
        	messageLogsTextArea.setText(internetConnector.getMessageLogs());
        	facebookButton.setDisable(false);
        	twitterButton.setDisable(false);
        } else {
        	messageLogsTextArea.setText(internetConnector.getMessageLogs());
        	facebookButton.setDisable(true);
        	twitterButton.setDisable(true);
        }
        
        facebookButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				BufferedWriter bw = null;
				FileWriter fwScraped = null;
				FileWriter fwStoppedWords = null;
				FileWriter fwNormalized = null;
				FileWriter fwEnglishWordsOnly = null;
				FileWriter fwCSVTraining = null;
				
				if ((profileIdTextField.getText().length() > 0 && !profileIdTextField.getText().equals("")) || (!yearComboBox.getSelectionModel().getSelectedItem().equals(""))) {
					String profileId = profileIdTextField.getText();
					String year = yearComboBox.getSelectionModel().getSelectedItem();
					String username = loadProperties.get("facebook.emailId");
					String password = loadProperties.get("facebook.password");
					String profileURL = loadProperties.get("facebook.urlAddress").concat("/").concat(profileId);
					String scrapedFilePathName = loadProperties.get("directory.scrapedFile")
															   .concat("\\facebook_profile_")
														       .concat(profileId)
														       .concat("_")
														       .concat(year)
														       .concat(".txt");
					String normalizedFilePathName = loadProperties.get("directory.slangWordFile")
															      .concat("\\facebook_profile_")
															      .concat(profileId)
															      .concat("_")
															      .concat(year)
															      .concat("_normalized.txt");
					String stoppedWordsFilePathName = loadProperties.get("directory.stopWordsFile")
															        .concat("\\facebook_profile_")
															        .concat(profileId)
															        .concat("_")
															        .concat(year)
															        .concat("_stoppedWords.txt");
					String englishWordsOnlyFilePathName = loadProperties.get("directory.noiseSpellFile")
						     											.concat("\\facebook_profile_")
																	    .concat(profileId)
																	    .concat("_")
																	    .concat(year)
																	    .concat("_final.txt");
					String csvTrainingFilePathName = loadProperties.get("directory.noiseSpellFile")
																   .concat("\\facebook_profile_")
																   .concat(profileId)
																   .concat("_")
																   .concat(year)
																   .concat("_training.csv");
					
					String timelineContent = "";
					
					File scrapedFile = new File(scrapedFilePathName);
					File normalizedFile = new File(normalizedFilePathName);
					File stoppedWordsFile = new File(stoppedWordsFilePathName);
					File englishWordsOnlyFile = new File(englishWordsOnlyFilePathName);
					File csvTrainingFile = new File(csvTrainingFilePathName);
					
					ProfileScraper ps = new ProfileScraper();
					WordPreProcessor wpp = new WordPreProcessor();
					
					// 1.  Web scraped the given profile, download, and saved it in a given file...
					try {
						if (!scrapedFile.exists()) {
							scrapedFile.createNewFile();
							messageLogsTextArea.appendText("[INFO]: Creating a new file under ".concat(scrapedFilePathName).concat("\n"));
						}
						
						timelineContent = ps.scrapeFacebookTimelinePosts(profileURL, Long.parseLong(year), username, password);
						
						if (timelineContent != null && !timelineContent.isEmpty()) {
							messageLogsTextArea.appendText("[INFO]: Writing content in ".concat(scrapedFilePathName).concat(".\n"));
							messageLogsTextArea.appendText(ps.getMessageLogs());
							
							fwScraped = new FileWriter(scrapedFile.getAbsolutePath(), true);
							
							bw = new BufferedWriter(fwScraped);
							bw.write(timelineContent);
							bw.flush();
							
							messageLogsTextArea.appendText("[INFO]: Done writing content in ".concat(scrapedFilePathName).concat(".\n"));
						}
					} catch (IOException ioe) {
						messageLogsTextArea.appendText("[ERROR]: ".concat(ioe.getMessage()).concat("\n"));
					} finally {
						try {
							if (bw != null) {
								bw.close();
							}
							
							if (fwScraped != null) {
								fwScraped.close();
							}
						} catch (IOException ioe) {
							messageLogsTextArea.appendText("[ERROR]: ".concat(ioe.getMessage()).concat("\n"));
						}
					}
					
					// 2.  Read the scraped file and normalize the words.  Search the emojis and the internet lingo words and convert it into meaningful words.  Then save it into a new file...
					try {
						messageLogsTextArea.appendText("[INFO]: Normalizing the words in ".concat(scrapedFilePathName).concat(".\n"));
						messageLogsTextArea.appendText("[INFO]: ".concat(wpp.getMessageLogs()).concat(" ...\n"));
						
						String normalizedWords = wpp.applyWordNormalizer(scrapedFile.getAbsolutePath());
						
						fwNormalized = new FileWriter(normalizedFile, true);
						fwNormalized.write(normalizedWords);
						fwNormalized.flush();
						
						messageLogsTextArea.appendText("[INFO]: Done normalizing the words.  ".concat(normalizedFilePathName).concat(" file has been created.\n"));
					} catch (IOException ioe) {
						messageLogsTextArea.appendText("[ERROR]: ".concat(ioe.getMessage()).concat("\n"));
					} finally {
						try {
							if (fwNormalized != null) {
								fwNormalized.close();
							}
						} catch (IOException ioe) {
							messageLogsTextArea.appendText("[ERROR]: ".concat(ioe.getMessage()).concat("\n"));
						}
					}
					
					// 2.  Read the file that has already normalized its stop words.  After that, save it into a new file...
					try {
						messageLogsTextArea.appendText("[INFO]: Removing stop words in ".concat(normalizedFilePathName).concat(".\n"));
						messageLogsTextArea.appendText("[INFO]: ".concat(wpp.getMessageLogs()).concat(" ...\n"));
						
						String stoppedWords = wpp.applyStopWordsRemover(normalizedFile.getAbsolutePath());
						
						fwStoppedWords = new FileWriter(stoppedWordsFile, true);
						fwStoppedWords.write(stoppedWords);
						fwStoppedWords.flush();
						
						messageLogsTextArea.appendText("[INFO]: Done removing stop words! ".concat(stoppedWordsFilePathName).concat(" file has been created.\n"));
					} catch (IOException ioe) {
						messageLogsTextArea.appendText("[ERROR]: ".concat(ioe.getMessage()).concat("\n"));
					} finally {
						try {
							if (fwStoppedWords != null) {
								fwStoppedWords.close();
							}
						} catch (IOException ioe) {
							messageLogsTextArea.appendText("[ERROR]: ".concat(ioe.getMessage()).concat("\n"));
						}
					}
					
					// 4.  Read the file that has already been spell checked.  Remove all non-English words and save it in a file for final output...
					try {
						messageLogsTextArea.appendText("[INFO]: Removing non-English words in ".concat(stoppedWordsFilePathName).concat(".\n"));
						messageLogsTextArea.appendText("[INFO]: ".concat(wpp.getMessageLogs()).concat(" ...\n"));
						
						String englishWords = wpp.applyEnglishWordsOnly(stoppedWordsFile.getAbsolutePath());
						
						fwEnglishWordsOnly = new FileWriter(englishWordsOnlyFile, true);
						fwEnglishWordsOnly.write(englishWords);
						fwEnglishWordsOnly.flush();
						
						messageLogsTextArea.appendText("[INFO]: Done spell checking of words. ".concat(englishWordsOnlyFilePathName).concat(" file has been created.\n"));
					} catch (IOException ioe) {
						messageLogsTextArea.appendText("[ERROR]: ".concat(ioe.getMessage()).concat("\n"));
					} finally {
						try {
							if (fwEnglishWordsOnly != null) {
								fwEnglishWordsOnly.close();
							}
						} catch (IOException ioe) {
							messageLogsTextArea.appendText("[ERROR]: ".concat(ioe.getMessage()).concat("\n"));
						}
					}
					
					// 5.  Create the training data which will be used in our SVM...
					try {
						SVMProcessor svm = new SVMProcessor();
						String csvTraining = svm.generateCSVTrainingData(englishWordsOnlyFile.getAbsolutePath());
						
						fwCSVTraining = new FileWriter(csvTrainingFile, true);
						fwCSVTraining.write(csvTraining);
						fwCSVTraining.flush();
					} catch (IOException ioe) {
						System.err.println("[ERROR]: ".concat(ioe.getMessage()));
					} finally {
						try {
							if (fwCSVTraining != null) {
								fwCSVTraining.close();
							}
						} catch (IOException ioe) {
							System.err.println("[ERROR]: ".concat(ioe.getMessage()));
						}
						
						// Enable the Generate Output button after preprocessing of files...
						displayOutputButton.setDisable(false);
					}
				} else {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error Processing Profile.");
					alert.setHeaderText(null);
					alert.setContentText("Please provide the profile's user id and/or timeline year!");
					alert.showAndWait();
				}
			}
        });
        
        twitterButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Twitter Scraper Functionality.");
				alert.setHeaderText(null);
				alert.setContentText("Functionality for Twitter scraping is still ongoing.  Don't worry, it will be added soon :)");
				alert.showAndWait();
			}
        });
        
        databaseButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				DatabaseSettingsApp dbSetApp = new DatabaseSettingsApp();
				
				try {
					dbSetApp.start(dbSetApp.getStage());
				} catch (Exception e) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error Database Configuration.");
					alert.setHeaderText(null);
					alert.setContentText("Cannot display the database settings window!"
							.concat("\n[ERROR]: ")
							.concat(e.getMessage()));

					alert.showAndWait();
				}
			}
        });
        
        webButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				SocialMediaSettingsApp smSetApp = new SocialMediaSettingsApp();
				
				try {
					smSetApp.start(smSetApp.getStage());
				} catch (Exception e) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error Social Media Configuration.");
					alert.setHeaderText(null);
					alert.setContentText("Cannot display the social media settings window!"
							.concat("\n[ERROR]: ")
							.concat(e.getMessage()));
					alert.showAndWait();
				}
			}
        });
        
        directoryButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ProcessedDirectorySettingsApp pdSetApp = new ProcessedDirectorySettingsApp();
				
				try {
					pdSetApp.start(pdSetApp.getStage());
				} catch (Exception e) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error Processed Directory Configuration.");
					alert.setHeaderText(null);
					alert.setContentText("Cannot display the processed directory settings window!"
							.concat("\n[ERROR]: ")
							.concat(e.getMessage()));
					alert.showAndWait();
				}
			}
        });
        
        displayOutputButton.setOnAction(new EventHandler<ActionEvent>() {
			@SuppressWarnings("rawtypes")
			@Override
			public void handle(ActionEvent event) {
				WordPreProcessor wpp = new WordPreProcessor();
				
				String englishWordsOnlyFilePathName = loadProperties.get("directory.noiseSpellFile")
																	.concat("\\facebook_profile_")
																    .concat(profileIdTextField.getText().trim())
																    .concat("_")
																    .concat(yearComboBox.getSelectionModel().getSelectedItem())
																    .concat("_final.txt");
				String csvTrainingFilePathName = loadProperties.get("directory.noiseSpellFile")
															   .concat("\\facebook_profile_")
															   .concat(profileIdTextField.getText().trim())
															   .concat("_")
															   .concat(yearComboBox.getSelectionModel().getSelectedItem())
															   .concat("_training.csv");
				
				
				File finalFile = new File(englishWordsOnlyFilePathName);
				File trainingFile = new File(csvTrainingFilePathName);
				
				Map<String, Long> positiveMap = wpp.getMonthlyWordScore(finalFile.getAbsolutePath(), "positive");
				Map<String, Long> negativeMap = wpp.getMonthlyWordScore(finalFile.getAbsolutePath(), "negative");
				Map<String, Long> neutralMap = wpp.getNeutralWordScore(finalFile.getAbsolutePath());
				
				Tab resultTab = new Tab(profileIdTextField.getText().trim().concat(" - Word Count Report"));
				Tab trainingTab = new Tab(profileIdTextField.getText().trim().concat(" - Analysis Report"));
				
				CategoryAxis xAxis = new CategoryAxis();
				xAxis.setLabel("Month");
				
				NumberAxis yAxis = new NumberAxis();
				
				LineChart<String,Number> lineChart = new LineChart<String,Number>(xAxis,yAxis);
				lineChart.setTitle("Word Counts in Facebook Timeline for ".concat(yearComboBox.getSelectionModel().getSelectedItem()));
				
				XYChart.Series positiveSeries = new XYChart.Series();
				positiveSeries.setName("Positive Words");
				
				XYChart.Series negativeSeries = new XYChart.Series();
				negativeSeries.setName("Negative Words");
				
				XYChart.Series neutralSeries = new XYChart.Series();
				neutralSeries.setName("Neutral Words");
				
				positiveSeries.getData().add(new XYChart.Data("Jan", positiveMap.containsKey("January") ? positiveMap.get("January") : 0));
				positiveSeries.getData().add(new XYChart.Data("Feb", positiveMap.containsKey("February") ? positiveMap.get("February") : 0));
				positiveSeries.getData().add(new XYChart.Data("Mar", positiveMap.containsKey("March") ? positiveMap.get("March") : 0));
				positiveSeries.getData().add(new XYChart.Data("Apr", positiveMap.containsKey("April") ? positiveMap.get("April") : 0));
				positiveSeries.getData().add(new XYChart.Data("May", positiveMap.containsKey("May") ? positiveMap.get("May") : 0));
				positiveSeries.getData().add(new XYChart.Data("Jun", positiveMap.containsKey("June") ? positiveMap.get("June") : 0));
				positiveSeries.getData().add(new XYChart.Data("Jul", positiveMap.containsKey("July") ? positiveMap.get("July") : 0));
				positiveSeries.getData().add(new XYChart.Data("Aug", positiveMap.containsKey("August") ? positiveMap.get("August") : 0));
				positiveSeries.getData().add(new XYChart.Data("Sep", positiveMap.containsKey("September") ? positiveMap.get("September") : 0));
				positiveSeries.getData().add(new XYChart.Data("Oct", positiveMap.containsKey("October") ? positiveMap.get("October") : 0));
				positiveSeries.getData().add(new XYChart.Data("Nov", positiveMap.containsKey("November") ? positiveMap.get("November") : 0));
				positiveSeries.getData().add(new XYChart.Data("Dec", positiveMap.containsKey("December") ? positiveMap.get("December") : 0));
				
				negativeSeries.getData().add(new XYChart.Data("Jan", negativeMap.containsKey("January") ? negativeMap.get("January") : 0));
				negativeSeries.getData().add(new XYChart.Data("Feb", negativeMap.containsKey("February") ? negativeMap.get("February") : 0));
				negativeSeries.getData().add(new XYChart.Data("Mar", negativeMap.containsKey("March") ? negativeMap.get("March") : 0));
				negativeSeries.getData().add(new XYChart.Data("Apr", negativeMap.containsKey("April") ? negativeMap.get("April") : 0));
				negativeSeries.getData().add(new XYChart.Data("May", negativeMap.containsKey("May") ? negativeMap.get("May") : 0));
				negativeSeries.getData().add(new XYChart.Data("Jun", negativeMap.containsKey("June") ? negativeMap.get("June") : 0));
				negativeSeries.getData().add(new XYChart.Data("Jul", negativeMap.containsKey("July") ? negativeMap.get("July") : 0));
				negativeSeries.getData().add(new XYChart.Data("Aug", negativeMap.containsKey("August") ? negativeMap.get("August") : 0));
				negativeSeries.getData().add(new XYChart.Data("Sep", negativeMap.containsKey("September") ? negativeMap.get("September") : 0));
				negativeSeries.getData().add(new XYChart.Data("Oct", negativeMap.containsKey("October") ? negativeMap.get("October") : 0));
				negativeSeries.getData().add(new XYChart.Data("Nov", negativeMap.containsKey("November") ? negativeMap.get("November") : 0));
				negativeSeries.getData().add(new XYChart.Data("Dec", negativeMap.containsKey("December") ? negativeMap.get("December") : 0));
				
				neutralSeries.getData().add(new XYChart.Data("Jan", neutralMap.containsKey("January") ? neutralMap.get("January") : 0));
				neutralSeries.getData().add(new XYChart.Data("Feb", neutralMap.containsKey("February") ? neutralMap.get("February") : 0));
				neutralSeries.getData().add(new XYChart.Data("Mar", neutralMap.containsKey("March") ? neutralMap.get("March") : 0));
				neutralSeries.getData().add(new XYChart.Data("Apr", neutralMap.containsKey("April") ? neutralMap.get("April") : 0));
				neutralSeries.getData().add(new XYChart.Data("May", neutralMap.containsKey("May") ? neutralMap.get("May") : 0));
				neutralSeries.getData().add(new XYChart.Data("Jun", neutralMap.containsKey("June") ? neutralMap.get("June") : 0));
				neutralSeries.getData().add(new XYChart.Data("Jul", neutralMap.containsKey("July") ? neutralMap.get("July") : 0));
				neutralSeries.getData().add(new XYChart.Data("Aug", neutralMap.containsKey("August") ? neutralMap.get("August") : 0));
				neutralSeries.getData().add(new XYChart.Data("Sep", neutralMap.containsKey("September") ? neutralMap.get("September") : 0));
				neutralSeries.getData().add(new XYChart.Data("Oct", neutralMap.containsKey("October") ? neutralMap.get("October") : 0));
				neutralSeries.getData().add(new XYChart.Data("Nov", neutralMap.containsKey("November") ? neutralMap.get("November") : 0));
				neutralSeries.getData().add(new XYChart.Data("Dec", neutralMap.containsKey("December") ? neutralMap.get("December") : 0));
				
				lineChart.getData().addAll(positiveSeries, negativeSeries, neutralSeries);
				resultTab.setContent(lineChart);
				
				SVMProcessor svm = new SVMProcessor();
				Evaluation eval = svm.generateSVMResultFromTrainingData(trainingFile.getAbsolutePath());
				
				PieChart pieChart = new PieChart();
				
				try {
					PieChart.Data slice1 = new PieChart.Data("Depression", eval.correct());
			        PieChart.Data slice2 = new PieChart.Data("Anxiety" , eval.incorrect());
			        PieChart.Data slice3 = new PieChart.Data("Normality", eval.avgCost());
			        
			        pieChart.getData().add(slice1);
			        pieChart.getData().add(slice2);
			        pieChart.getData().add(slice3);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				trainingTab.setContent(pieChart);
				
				resultTabbedPane.getTabs().add(resultTab);
				resultTabbedPane.getTabs().add(trainingTab);
				resultTabbedPane.getSelectionModel().select(resultTab);
				
				displayOutputButton.setDisable(true);
				messageLogsTextArea.setText("");
				
				if (internetConnector.isInternetConnected()) {
		        	messageLogsTextArea.setText(internetConnector.getMessageLogs());
		        	facebookButton.setDisable(false);
		        	twitterButton.setDisable(false);
		        } else {
		        	messageLogsTextArea.setText(internetConnector.getMessageLogs());
		        	facebookButton.setDisable(true);
		        	twitterButton.setDisable(true);
		        }
			}
        });
        
        mainWindowStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				Platform.exit();
			}
        	
        });
        mainWindowStage.setScene(scene);
        mainWindowStage.show();
	}

}

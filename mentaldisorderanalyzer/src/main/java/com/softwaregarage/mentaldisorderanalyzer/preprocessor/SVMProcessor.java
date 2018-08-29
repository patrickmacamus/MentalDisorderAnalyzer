package com.softwaregarage.mentaldisorderanalyzer.preprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Random;

import com.softwaregarage.mentaldisorderanalyzer.util.ConfigReaderAndWriter;

import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;

public class SVMProcessor {

	private Connection connection;
	private StringBuilder sbLogs;
	private ConfigReaderAndWriter configReadAndWrite;
	
	public SVMProcessor() {
		sbLogs = new StringBuilder();
		configReadAndWrite = new ConfigReaderAndWriter();
		
		Map<String, String> properties = configReadAndWrite.readConfiguration();
		
		String dbURL = properties.get("mysql.urlConnection").concat("/").concat(properties.get("mysql.databaseName"));
		String username = properties.get("mysql.username");
		String password = properties.get("mysql.password");
		
		// This will connect to our MySQL local database...
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection(dbURL, username, password);
		} catch (ClassNotFoundException ex) {
			sbLogs.append("[ERROR]: Cannot find the JDBC driver class for MySQL!\n");
		} catch (SQLException ex) {
			sbLogs.append("[ERROR]: Cannot connect to MySQL database!\n");
		}
	}
	
	@SuppressWarnings("resource")
	public String generateCSVTrainingData(String fileName) {
		StringBuilder contentBuilder = new StringBuilder();
		contentBuilder.append("disorder_word,disorder_type,disorder_score").append("\n");
		
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
			String currentLine;
			
			PreparedStatement psForDisorder = connection.prepareStatement("SELECT * FROM nlpd_disorder WHERE disorder_word = ?");
			
			while ((currentLine = bufferedReader.readLine()) != null) {
				String[] words = currentLine.split("\\s");
				StringBuilder processedLine = new StringBuilder();
				
				for (int x=0; x < words.length; x++) {
					psForDisorder.setString(1, words[x]);
					ResultSet rs = psForDisorder.executeQuery();
					
					while (rs.next()) {
						processedLine.append(words[x]).append(",")
									 .append(rs.getString("disorder_type")).append(",")
									 .append(rs.getString("disorder_level")).append("\n");
						
					}
				}
				
				contentBuilder.append(processedLine.toString());
			}
		} catch (IOException ex) {
			sbLogs.append("[ERROR]: Cannot read the text file!\n");
			sbLogs.append("[ERROR]: ".concat(ex.getMessage()) + "\n");
		} catch (SQLException ex) {
			sbLogs.append("[ERROR]: Error in querying the natural language processing tables!\n");
			sbLogs.append("[ERROR]: ".concat(ex.getMessage()) + "\n");
		}
		
		sbLogs.append("[INFO]: Done generating training file...");
		
		return contentBuilder.toString();
	}
	
	public Evaluation generateSVMResultFromTrainingData(String fileName) {
		try {
			Instances train = DataSource.read(fileName);			
			train.setClassIndex(train.numAttributes()-1);
			
			NumericToNominal ntn = new NumericToNominal();
			ntn.setInputFormat(train);
			
			train = Filter.useFilter(train, ntn);
			
			LibSVM svm = new LibSVM();
			svm.setSVMType(new SelectedTag(LibSVM.SVMTYPE_C_SVC, LibSVM.TAGS_SVMTYPE));
			svm.setBatchSize("100");
			svm.setCacheSize(40.0);
			svm.setCoef0(0.0);
			svm.setCost(1.0);
			svm.setDebug(false);
			svm.setDegree(3);
			svm.setDoNotCheckCapabilities(false);
			svm.setDoNotReplaceMissingValues(false);
			svm.setEps(0.001);
			svm.setGamma(0.0);
			svm.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_LINEAR, LibSVM.TAGS_KERNELTYPE));
			svm.setLoss(0.1);
			svm.setModelFile(new File("C:\\Program Files\\Weka 3.8"));
			svm.setNormalize(false);
			svm.setNu(0.5);
			svm.setNumDecimalPlaces(2);
			svm.setProbabilityEstimates(false);
			svm.setSeed(1);
			svm.setShrinking(true);
			
			svm.buildClassifier(train);
			
			Evaluation eval = new Evaluation(train);
			eval.crossValidateModel(svm, train, 10, new Random(1));
			
			return eval;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return null;
		}
	}
	
}

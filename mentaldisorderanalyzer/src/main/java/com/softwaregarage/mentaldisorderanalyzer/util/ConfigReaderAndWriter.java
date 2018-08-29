package com.softwaregarage.mentaldisorderanalyzer.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class ConfigReaderAndWriter {
	
	private PropertiesConfiguration propertiesConfig;
	private String messageLogs;
	
	/**
	 * Class constructor that holds the initialization of the 
	 * required classes for reading and writing configuration file.
	 */
	public ConfigReaderAndWriter() {
		messageLogs = "";
		
		try {
			propertiesConfig = new PropertiesConfiguration("mentaldisorderanalyzer.config.properties");
		} catch (ConfigurationException e) {
			messageLogs = messageLogs.concat("[ERROR]: ")
								.concat(e.getMessage())
								.concat("\n");
		}
	}
	
	/**
	 * This method will write a map of configurations properties 
	 * to our mentaldisorderanalyzer.config.properties file.
	 * 
	 * @param configurations - A map of configuration properties.
	 */
	public void writeConfiguration(Map<String, String> configurations) {
		if ((configurations != null) && (configurations.size() != 0)) {
			try {
				Iterator<String> keyIterator = configurations.keySet().iterator();
				
				while (keyIterator.hasNext()) {
					String key = (String) keyIterator.next();
					String value = configurations.get(key);
					
					propertiesConfig.setProperty(key, value);
				}
				
				propertiesConfig.save();
			} catch (ConfigurationException ce) {
				messageLogs = messageLogs.concat("[ERROR]: Problem writing to our properties file.")
									.concat("\n")
									.concat("[ERROR]: ")
									.concat(ce.getMessage())
									.concat("\n");
			} 
		}
	}
	
	/**
	 * This method will return a map of configuration properties 
	 * from our mentaldisorderanalyzer.config.properties file.
	 * 
	 * @return Map of configuration properties.
	 */
	public Map<String, String> readConfiguration() {
		Map<String, String> configs = null;
		
		Iterator<String> keyIterator = propertiesConfig.getKeys();
		configs = new HashMap<String, String>();
		
		while (keyIterator.hasNext()) {
			String key = keyIterator.next();
			String value = propertiesConfig.getString(key);
			
			configs.put(key, value);
		}
		
		return configs;
	}
	
	/**
	 * Gets the appended message logs generated 
	 * from ConfigReaderAndWriter.java file.
	 * 
	 * @return The appended message logs.
	 */
	public String getMessageLogs() {
		return messageLogs;
	}
	
}

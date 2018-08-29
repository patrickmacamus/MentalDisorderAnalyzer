package com.softwaregarage.mentaldisorderanalyzer.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class InternetConnector {

	private StringBuilder sbLogs;
	
	public InternetConnector() {
		sbLogs = new StringBuilder();
	}
	
	/**
	 * This will check if you are connected in the internet.
	 * 
	 * @return <b>True</b> if you are connected.  Otherwise, <b>false</b>.
	 */
	public boolean isInternetConnected() {
		try {
			URL url = new URL("https://www.google.com.ph");
			
			URLConnection urlConnection = url.openConnection();
			urlConnection.connect();	
			
			sbLogs.append("[INFO]: You are connected to the internet.\n");
			
			return true;
		} catch (Exception exception) {
			sbLogs.append("[ERROR]: You are not connected to the internet.  Please check your connection.\n");
			return false;
		}
	}
	
	/**
	 * This will check the status of your internet connection.
	 */
	public void checkInternetConnectionStatus() {
		try {
			Process process = java.lang.Runtime.getRuntime().exec("ping www.google.com");
			int status = process.waitFor();
			
			if (status == 0) {
				sbLogs.append("[INFO]: Internet connection is good.\n");
			} else {
				sbLogs.append("[INFO]: Internet connection is bad.\n");
			}
		} catch (IOException | InterruptedException e) {
			sbLogs.append("[ERROR]: ").append(e.getMessage()).append("\n");
		}
	}
	
	/**
	 * Prints the message logs.
	 * @return The message logs.
	 */
	public String getMessageLogs() {
		return sbLogs.toString();
	}
	
}

package com.softwaregarage.mentaldisorderanalyzer.preprocessor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import com.softwaregarage.mentaldisorderanalyzer.util.ConfigReaderAndWriter;

public class WordPreProcessor {

	private Connection connection;
	private StringBuilder sbLogs;
	private ConfigReaderAndWriter configReadAndWrite;
	
	public WordPreProcessor() {
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
	
	/**
	 * This method will check if a certain String word is an emoji.
	 * It uses a regular expression that patterns to a set of unicode values. 
	 * 
	 * @param word - The input value.
	 * @return Returns true if the word is an emoji.
	 */
	private boolean isWordAnEmoji(String word) {
		String emojiRegex = "(?:[\\u2700-\\u27bf]|" +
		        "(?:[\\ud83c\\udde6-\\ud83c\\uddff]){2}|" +
		        "[\\ud800\\udc00-\\uDBFF\\uDFFF]|[\\u2600-\\u26FF])[\\ufe0e\\ufe0f]?(?:[\\u0300-\\u036f\\ufe20-\\ufe23\\u20d0-\\u20f0]|[\\ud83c\\udffb-\\ud83c\\udfff])?" +
		        "(?:\\u200d(?:[^\\ud800-\\udfff]|" +
		        "(?:[\\ud83c\\udde6-\\ud83c\\uddff]){2}|" +
		        "[\\ud800\\udc00-\\uDBFF\\uDFFF]|[\\u2600-\\u26FF])[\\ufe0e\\ufe0f]?(?:[\\u0300-\\u036f\\ufe20-\\ufe23\\u20d0-\\u20f0]|[\\ud83c\\udffb-\\ud83c\\udfff])?)*|" +
		        "[\\u0023-\\u0039]\\ufe0f?\\u20e3|\\u3299|\\u3297|\\u303d|\\u3030|\\u24c2|[\\ud83c\\udd70-\\ud83c\\udd71]|[\\ud83c\\udd7e-\\ud83c\\udd7f]|\\ud83c\\udd8e|[\\ud83c\\udd91-\\ud83c\\udd9a]|[\\ud83c\\udde6-\\ud83c\\uddff]|[\\ud83c\\ude01-\\ud83c\\ude02]|\\ud83c\\ude1a|\\ud83c\\ude2f|[\\ud83c\\ude32-\\ud83c\\ude3a]|[\\ud83c\\ude50-\\ud83c\\ude51]|\\u203c|\\u2049|[\\u25aa-\\u25ab]|\\u25b6|\\u25c0|[\\u25fb-\\u25fe]|\\u00a9|\\u00ae|\\u2122|\\u2139|\\ud83c\\udc04|[\\u2600-\\u26FF]|\\u2b05|\\u2b06|\\u2b07|\\u2b1b|\\u2b1c|\\u2b50|\\u2b55|\\u231a|\\u231b|\\u2328|\\u23cf|[\\u23e9-\\u23f3]|[\\u23f8-\\u23fa]|\\ud83c\\udccf|\\u2934|\\u2935|[\\u2190-\\u21ff]";
		
		Pattern emojiPattern = Pattern.compile(emojiRegex);
		Matcher matcher = emojiPattern.matcher(word);
		
		return matcher.find();
	}
	
	/**
	 * This method will remove all special characters in a word.
	 * 
	 * @param word - The word that has a special characters.
	 * @return A word in which the special characters has already been removed.
	 */
	public String removeSpecialCharacters(String word) {
		char[] wordChars = word.toCharArray();
				
		Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
		StringBuilder newWord = new StringBuilder();
		
		for (int x = 0; x < wordChars.length; x++) {
			String wordChar = String.valueOf(wordChars[x]);
			Matcher match = pattern.matcher(wordChar);
						
			if (match.find()) {
				wordChar = wordChar.replaceAll("\\".concat(match.group()), "");
			}
			
			newWord.append(wordChar);
		}
		
		return newWord.toString();		
	}
	
	/**
	 * This method will normalize all the words in a scraped file 
	 * by converting all emojis and internet lingos into meaningful words.
	 * 
	 * @param fileName - The name of the scraped file to be normalized.
	 * @return Texts with all emojis and internet lingos were already converted.
	 */
	@SuppressWarnings("resource")
	public String applyWordNormalizer(String fileName) {
		StringBuilder contentBuilder = new StringBuilder();
		
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
			String currentLine;
			
			PreparedStatement psForEmojis = connection.prepareStatement("SELECT * FROM nlpd_emojis WHERE emoji_code LIKE ?");
			PreparedStatement psForInternetLingo = connection.prepareStatement("SELECT * FROM nlpd_internet_lingo_words WHERE slang_word LIKE ?");
			
			while ((currentLine = bufferedReader.readLine()) != null) {
				String[] words = currentLine.split("\\s");
				StringBuilder processedLine = new StringBuilder();
				
				for (int x=0; x < words.length; x++) {
					if (isWordAnEmoji(words[x])) {
						// This portion will check if there are multiple emojis found in a word...
						String[] wordEmojis = words[x].split("\\\\");
						
						if (wordEmojis.length > 1) {
							for (int y=0; y < wordEmojis.length; y++) {
								if (isWordAnEmoji(wordEmojis[y])) {
									String unicodeValue = StringEscapeUtils.escapeJava(wordEmojis[y]);
									
									psForEmojis.setString(1, "%"+unicodeValue+"%");
									ResultSet rs = psForEmojis.executeQuery();
									
									while (rs.next()) {
										wordEmojis[y] = rs.getString("emoji_name");
									}
									
									processedLine.append(wordEmojis[y]).append(" ");
								}
							}
						} else {
							String unicodeValue = StringEscapeUtils.escapeJava(words[x]);
							
							psForEmojis.setString(1, "%"+unicodeValue+"%");
							ResultSet rs = psForEmojis.executeQuery();
							
							while (rs.next()) {
								words[x] = rs.getString("emoji_name");
							}
						}
					} else {
						psForInternetLingo.setString(1, words[x]);
						ResultSet rs = psForInternetLingo.executeQuery();
						
						while (rs.next()) {
							words[x] = rs.getString("description");
						}
					}
					
					if ((x+1) == words.length) {
						processedLine.append(words[x]);
					} else {
						processedLine.append(words[x]).append(" ");
					}
				}
				
				contentBuilder.append(processedLine.toString()).append("\n");
			}
		} catch (IOException ex) {
			sbLogs.append("[ERROR]: Cannot read the text file!\n");
			sbLogs.append("[ERROR]: ".concat(ex.getMessage()) + "\n");
		} catch (SQLException ex) {
			sbLogs.append("[ERROR]: Error in querying the natural language processing tables!\n");
			sbLogs.append("[ERROR]: ".concat(ex.getMessage()) + "\n");
		}
		
		sbLogs.append("[INFO]: Output is...\n");
		
		return contentBuilder.toString();
	}
	
	/**
	 * This method will remove all the stop words like "an", "a", "or",  
	 * special characters like "&", "$", "@" and escape characters like "\t", "\b", "\r".
	 *  
	 * @param fileName - The name of the normalized file.
	 * @return Texts that all stop words are removed.
	 */
	@SuppressWarnings("resource")
	public String applyStopWordsRemover(String fileName) {
		StringBuilder contentBuilder = new StringBuilder();
		
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
			String currentLine;
			
			PreparedStatement psForStopWords = connection.prepareStatement("SELECT * FROM nlpd_stop_words WHERE stop_word = ?");
			
			while ((currentLine = bufferedReader.readLine()) != null) {
				String[] words = currentLine.split("\\s");
				StringBuilder processedLine = new StringBuilder();
				
				for (int x=0; x < words.length; x++) {
					if (!isWordAnEmoji(words[x].trim())) {
						String removedSpecialChars = removeSpecialCharacters(words[x]);
						
						psForStopWords.setString(1, removedSpecialChars);
						ResultSet rs = psForStopWords.executeQuery();
						
						if (rs.next()) {
							processedLine.append("");
						} else {
							if ((x+1) == words.length) {
								processedLine.append(removedSpecialChars);
							} else {
								processedLine.append(removedSpecialChars).append(" ");
							}
						}
					}
				}
				
				contentBuilder.append(processedLine).append("\n");
			}
		} catch (IOException ex) {
			sbLogs.append("[ERROR]: Cannot read the text file!\n");
			sbLogs.append("[ERROR]: ".concat(ex.getMessage()) + "\n");
		} catch (SQLException ex) {
			sbLogs.append("[ERROR]: Error in querying the natural language processing tables!\n");
			sbLogs.append("[ERROR]: ".concat(ex.getMessage()) + "\n");
		}
		
		sbLogs.append("[INFO]: Output is...\n");
		
		return contentBuilder.toString();
	}
	
	/**
	 * This method will remove all the non-English words found in the file.
	 * 
	 * @param fileName - The name of the scraped file where non-English words will be removed.
	 * @return Texts the contains only English words.
	 */
	@SuppressWarnings("resource")
	public String applyEnglishWordsOnly(String fileName) {
		StringBuilder contentBuilder = new StringBuilder();
		
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
			String currentLine;
			
			PreparedStatement psForEnglishWords = connection.prepareStatement("SELECT * FROM nlpd_entries WHERE word = ?");
			
			while ((currentLine = bufferedReader.readLine()) != null) {
				String[] words = currentLine.split("\\s");
				StringBuilder processedLine = new StringBuilder();
				
				for (int x=0; x < words.length; x++) {
					if (!isWordAnEmoji(words[x])) {
						psForEnglishWords.setString(1, words[x]);
						ResultSet rs = psForEnglishWords.executeQuery();
						
						if (rs.next()) {
							if ((x+1) == words.length) {
								processedLine.append(words[x]);
							} else {
								processedLine.append(words[x]).append(" ");
							} 
						} 
					}
				}
				
				contentBuilder.append(processedLine.toString()).append("\n");
			}
		} catch (IOException ex) {
			sbLogs.append("[ERROR]: Cannot read the text file!\n");
			sbLogs.append("[ERROR]: ".concat(ex.getMessage()) + "\n");
		} catch (SQLException ex) {
			sbLogs.append("[ERROR]: Error in querying the natural language processing tables!\n");
			sbLogs.append("[ERROR]: ".concat(ex.getMessage()) + "\n");
		}
		
		sbLogs.append("[INFO]: Output is...\n");
		
		return contentBuilder.toString();
	}
	
	@SuppressWarnings("resource")
	public Map<String, Long> getMonthlyWordScore(String fileName, String polarityScore) {
		Map<String, Long> monthlyWordScoreMap = new HashMap<String, Long>();
		
		try {
			PreparedStatement psLexiconWords = connection.prepareStatement("SELECT * FROM nlpd_lexicon WHERE lexicon_word = ?");
			
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
			String currentLine;
			String month = "";
			long count = 0;
			
			while ((currentLine = bufferedReader.readLine()) != null) {
				String[] words = currentLine.split("\\s");
				
				for (int x=0; x < words.length; x++) {
					if (words[x].equals("January") || words[x].equals("February") || words[x].equals("March") 
							|| words[x].equals("April") || words[x].equals("May") || words[x].equals("June")
							|| words[x].equals("July") || words[x].equals("August") || words[x].equals("September") 
							|| words[x].equals("October") || words[x].equals("November") || words[x].equals("December")) {
						month = words[x];
					} else {
						psLexiconWords.setString(1, words[x]);
						ResultSet rs = psLexiconWords.executeQuery();
							
						while (rs.next()) {
							if (polarityScore.equals(rs.getString("lexicon_word_score"))) {
								count++;
							}
						}
					}
				}
				
				if (!month.equals("")) {
					if (monthlyWordScoreMap.containsKey(month)) {
						monthlyWordScoreMap.replace(month, monthlyWordScoreMap.get(month), count);
					} else {
						monthlyWordScoreMap.put(month, Long.valueOf(count));
					}
				} else {
					continue;
				}
			}
		} catch (IOException ex) {
			sbLogs.append("[ERROR]: Cannot read the text file!\n");
			sbLogs.append("[ERROR]: ".concat(ex.getMessage()) + "\n");
		} catch (SQLException ex) {
			sbLogs.append("[ERROR]: Error in querying the natural language processing tables!\n");
			sbLogs.append("[ERROR]: ".concat(ex.getMessage()) + "\n");
		}
		
		return monthlyWordScoreMap;
	}
	
	@SuppressWarnings("resource")
	public Map<String, Long> getNeutralWordScore(String fileName) {
		Map<String, Long> neutralWordScoreMap = new HashMap<String, Long>();
		
		try {
			PreparedStatement psLexiconWords = connection.prepareStatement("SELECT * FROM nlpd_lexicon WHERE lexicon_word = ?");
			
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
			String currentLine;
			String month = "";
			long count = 0;
			
			while ((currentLine = bufferedReader.readLine()) != null) {
				String[] words = currentLine.split("\\s");
				
				for (int x=0; x < words.length; x++) {
					if (words[x].equals("January") || words[x].equals("February") || words[x].equals("March") 
							|| words[x].equals("April") || words[x].equals("May") || words[x].equals("June")
							|| words[x].equals("July") || words[x].equals("August") || words[x].equals("September") 
							|| words[x].equals("October") || words[x].equals("November") || words[x].equals("December")) {
						month = words[x];
					} else {
						psLexiconWords.setString(1, words[x]);
						ResultSet rs = psLexiconWords.executeQuery();
							
						if (!rs.next()) {
							count++;
						}
					}
				}
				
				if (!month.equals("")) {
					if (neutralWordScoreMap.containsKey(month)) {
						neutralWordScoreMap.replace(month, neutralWordScoreMap.get(month), count);
					} else {
						neutralWordScoreMap.put(month, Long.valueOf(count));
					}
				} else {
					continue;
				}
			}
		} catch (IOException ex) {
			sbLogs.append("[ERROR]: Cannot read the text file!\n");
			sbLogs.append("[ERROR]: ".concat(ex.getMessage()) + "\n");
		} catch (SQLException ex) {
			sbLogs.append("[ERROR]: Error in querying the natural language processing tables!\n");
			sbLogs.append("[ERROR]: ".concat(ex.getMessage()) + "\n");
		}
		
		return neutralWordScoreMap;
	}
	
	public String getMessageLogs() {
		return sbLogs.toString();
	}
}

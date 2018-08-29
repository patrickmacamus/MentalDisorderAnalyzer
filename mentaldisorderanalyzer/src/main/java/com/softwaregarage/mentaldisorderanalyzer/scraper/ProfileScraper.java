package com.softwaregarage.mentaldisorderanalyzer.scraper;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

public class ProfileScraper {
	
	private WebClient webClient;
	
	private HtmlPage facebookLoginPage;
	private HtmlPage facebookProfilePage;
	private HtmlPage twitterLoginPage;
	private HtmlPage twitterHomePage;
//	private HtmlPage twitterProfilePage;
	
	private HtmlForm facebookLoginForm;
	private HtmlForm twitterLoginForm;
//	private HtmlForm twitterSearchForm;
	
	private HtmlTextInput facebookUsername;
	private HtmlTextInput twitterUsername;
//	private HtmlTextInput twitterSearch;
	
	private HtmlPasswordInput facebookPassword;
	private HtmlPasswordInput twitterPassword;
	
	private HtmlAnchor facebookYearPostLink;
	
	private List<HtmlDivision> facebookPostDivs;
	
	private StringBuilder sbLogs;
	
	/**
	 * Default class constructor...
	 */
	public ProfileScraper() {
		sbLogs = new StringBuilder();
	}
	
	/**
	 * This method will check if there is a "Show more" link in the Facebook profile page.
	 * 
	 * @param page - The Facebook profile page.
	 * @return Returns true if it has "Show more" link.  Otherwise, false.
	 */
	private boolean doesFacebookHasShowMoreLink(HtmlPage page) {
		HtmlAnchor showMoreLink = (HtmlAnchor) page.getAnchorByText("Show more".trim());
		
		if (showMoreLink != null) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * This method will scrape a certain Facebook profile page of the user.
	 * It will get all the texts in the timeline posts.
	 * 
	 * @param profileURL - The URL of the user's Facebook profile page.
	 * @param year - The year of timeline posts that you want to scrape.
	 * @param username - User credentials to access the certain Facebook profile.
	 * @param password - User credentials to access the certain Facebook profile.
	 * @return The whole scraped texts from the Facebook profile timelines.
	 */
	@SuppressWarnings("unchecked")
	public String scrapeFacebookTimelinePosts(String profileURL, long year, String username, String password) {
		StringBuilder sb = new StringBuilder();
		
		try {
			webClient = new WebClient(BrowserVersion.getDefault());
			webClient.getOptions().setJavaScriptEnabled(true);
			webClient.getOptions().setThrowExceptionOnScriptError(false);
			webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
			webClient.waitForBackgroundJavaScript(30*60000);
			webClient.setAjaxController(new NicelyResynchronizingAjaxController());
			
			facebookLoginPage = webClient.getPage("https://web.facebook.com/login.php?login_attempt=1&lwv=111");
			
			facebookLoginForm = (HtmlForm) facebookLoginPage.getElementById("login_form");
			facebookUsername = facebookLoginForm.getInputByName("email");
			facebookUsername.setValueAttribute(username);
			facebookPassword = facebookLoginForm.getInputByName("pass");
			facebookPassword.setValueAttribute(password);
			
			facebookLoginPage = (HtmlPage) facebookLoginForm.getButtonByName("login").click();
			facebookProfilePage = webClient.getPage(profileURL);
			
			try {
				facebookYearPostLink = (HtmlAnchor) facebookProfilePage.getAnchorByText(String.valueOf(year));
				
				HtmlPage profilePage = facebookYearPostLink.click();
				
				if (profilePage!= null) {
					facebookPostDivs = (List<HtmlDivision>) profilePage.getByXPath("//div[@role='article']");
					
					if (!facebookPostDivs.isEmpty()) {
						for (HtmlDivision facebookPostDiv : facebookPostDivs) {
							sb.append(facebookPostDiv.asText())
							  .append("\n\n\n");
						}
					} else {
						sbLogs.append("[INFO]: No posts found.\n");
					}
					
					HtmlPage showMorePostPage = profilePage;
					
					while (doesFacebookHasShowMoreLink(showMorePostPage)) {
						try {
							HtmlAnchor showMoreLink = (HtmlAnchor) showMorePostPage.getAnchorByText("Show more".trim());
							
							if (showMoreLink != null) {
								sbLogs.append("[INFO]: Link is ".concat(showMoreLink.getHrefAttribute()).concat("\n"));
								
								showMorePostPage = showMoreLink.click();
								
								if (showMorePostPage != null) {
									facebookPostDivs = (List<HtmlDivision>) showMorePostPage.getByXPath("//div[@role='article']");
									
									if (!facebookPostDivs.isEmpty()) {
										for (HtmlDivision facebookPostDiv : facebookPostDivs) {
											sb.append(facebookPostDiv.asText())
											  .append("\n\n\n");
										}
									} else {
										sbLogs.append("[INFO]: No more posts found.\n");
									}
									
									continue;
								} else {
									break;
								}
							} else {
								break;
							}
						} catch (ElementNotFoundException ex) {
							break;
						}
					}
				} else {
					sbLogs.append("[ERROR]: Cannot retrieved the page from the given year.\n");
				}
			} catch (ElementNotFoundException ex) {
				sbLogs.append("[INFO]: No link found for ".concat(String.valueOf(year)).concat("\n"));
			}	
		} catch (IOException exception) {
			sbLogs.append("[ERROR]: ".concat(exception.getMessage()).concat("\n"));
		}
		
		return sb.toString();
	}
	
	public String scrapeTwitterTimelineTweets(String profileName, long year, String username, String password) {
		StringBuilder sb = new StringBuilder();
		
		try {
			webClient = new WebClient(BrowserVersion.FIREFOX_17);
			
			WebRequest webRequest = new WebRequest(new URL("https://twitter.com/login"));
			
			webClient.getOptions().setCssEnabled(true);
			webClient.getOptions().setJavaScriptEnabled(true);
			webClient.getOptions().setThrowExceptionOnScriptError(false);
			webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
			webClient.getOptions().setTimeout(0);
			webClient.setJavaScriptTimeout(10000);
			webClient.setAjaxController(new NicelyResynchronizingAjaxController());
			
			try {
				twitterLoginPage = webClient.getPage(webRequest);
			} catch (IOException | FailingHttpStatusCodeException ex) {
				sbLogs.append("[ERROR]: ").append(ex.getMessage()).append("\n");
			}
			
			twitterLoginForm = (HtmlForm) twitterLoginPage.getByXPath("//form[@class='t1-form clearfix signin js-signin']").get(0);
			twitterUsername = twitterLoginForm.getInputByName("session[username_or_email]");
			twitterUsername.setValueAttribute(username);
			twitterPassword = twitterLoginForm.getInputByName("session[password]");
			twitterPassword.setValueAttribute(password);
			
			try {
				twitterHomePage = (HtmlPage) ((HtmlButton) twitterLoginForm.getElementsByTagName("button").get(0)).click();
			} catch (IOException ex) {
				sbLogs.append("[ERROR]: ").append(ex.getMessage()).append("\n");
			}
			
			synchronized (twitterHomePage) {
				try {
					twitterHomePage.wait(10000);
				} catch (InterruptedException ex) {
					sbLogs.append("[ERROR]: ").append(ex.getMessage()).append("\n");
				}
			}
						
//			twitterSearchForm = (HtmlForm) twitterHomePage.getByXPath("//form[@class='t1-form form-search js-search-form']").get(0);
//			twitterSearch = twitterSearchForm.getInputByName("q");
//			twitterSearch.setValueAttribute("from:".concat(profileName).concat(" since:").concat(String.valueOf(year)).concat("-01-01 until:").concat(String.valueOf(year)).concat("-12-31"));
//			
//			HtmlButton twitterSearchButton = (HtmlButton) twitterSearchForm.getElementsByTagName("button").get(0);
//			
//			twitterProfilePage = (HtmlPage) twitterSearchButton.click();
//			sb.append(twitterProfilePage.asXml());
			sb.append(twitterHomePage.asXml());
		} catch (IOException ioe) {
			sbLogs.append("[ERROR]: ".concat(ioe.getMessage()).concat("\n"));
		}
		
		return sb.toString();
	}
	
	public String getMessageLogs() {
		return sbLogs.toString();
	}
	
}

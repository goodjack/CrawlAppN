package com.eopcon.crawler.samsungcnt.util;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

public class SeleniumConnect {
	
	//private WebDriver driver;
	public String getPhantomJSConnect(String url) {
//		DesiredCapabilities caps = new DesiredCapabilities();
//		caps.setJavascriptEnabled(true);
//		caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, "C:/phantomjs/bin/phantomjs.exe");
		
		//linux
		System.setProperty("webdriver.chrome.driver", "/home/ubuntu/selenium/driver/chromedriver");
		//windows
		//System.setProperty("webdriver.chrome.driver", "C:/chrome/chromedriver.exe");
		ChromeOptions chromeOptions = new ChromeOptions();
		//windows
		//chromeOptions.setBinary("C:/chrome/chromedriver.exe");
		//linux
		chromeOptions.setBinary("/opt/google/chrome/google-chrome");
		
		chromeOptions.addArguments("--headless");
		WebDriver driver = new ChromeDriver(chromeOptions);
		
		//WebDriver driver = new ChromeDriver(options);
		//WebDriver driver = new PhantomJSDriver(caps);
	    driver.get(url);
	    JavascriptExecutor executor = (JavascriptExecutor)driver;
	    executor.executeScript("return typeof jQuery != 'undefined';");
	    String htmlContent = driver.getPageSource();
	    driver.close();
	    driver.quit();

	    return htmlContent;
	}
}

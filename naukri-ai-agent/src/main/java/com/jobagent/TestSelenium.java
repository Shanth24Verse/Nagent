package com.jobagent;

import org.openqa.selenium.WebDriver;

public class TestSelenium {
    public static void main(String[] args) {
        System.out.println("Starting Chrome with BotProfile...");
        
        WebDriver driver = DriverManager.getDriver();
        
        try {
            driver.get("https://www.naukri.com/nlogin/login");
            System.out.println("Opened Page Title: " + driver.getTitle());
            System.out.println("--> Please log into Naukri in the newly opened automated browser window now.");
            
            // Wait 30 seconds to allow one-time manual login
            Thread.sleep(30000); 
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            driver.quit();
            System.out.println("Browser closed successfully.");
        }
    }
}
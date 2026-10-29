package com.jobagent;

import java.util.Collections;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class DriverManager {

    public static WebDriver getDriver() {
        ChromeOptions options = new ChromeOptions();

        // 1. Dedicated Bot Profile Directory
        options.addArguments("--user-data-dir=C:/Users/Admin/AppData/Local/Google/Chrome/BotProfile");

        // 2. Start maximized to ensure full window view
        options.addArguments("--start-maximized");

        // 3. Anti-bot detection & log cleanup flags
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--log-level=3"); // Suppresses DevTools/CDP warnings
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));

        return new ChromeDriver(options);
    }
}
package com.jobagent;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

public class MainAgent {
    public static void main(String[] args) {
        System.out.println("=== Starting Naukri AI Application Agent ===");

        String profileJson = "";
        try {
            profileJson = new String(Files.readAllBytes(Paths.get("user_profile.json")));
            System.out.println("Loaded User Profile for: <>");
        } catch (Exception e) {
            System.err.println("Could not load user_profile.json: " + e.getMessage());
        }

        WebDriver driver = DriverManager.getDriver();
        JobService jobService = new JobService();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(8));

        try {
            // Search 3 pages with a 7-day freshness filter
            String targetKeyword = "Automation Testing";
            List<JobService.JobDetails> scrapedJobs = jobService.searchJobs(driver, targetKeyword, 3, 7);

            System.out.println("\n--- Evaluating & Applying to Jobs ---");
            for (JobService.JobDetails job : scrapedJobs) {
                if (DatabaseManager.isJobAlreadyApplied(job.jobId)) {
                    System.out.println("[SKIP - ALREADY APPLIED IN DB] ID: " + job.jobId + " | " + job.title);
                    continue;
                }

                int score = AIService.evaluateMatch(job.title, job.company, job.title, profileJson);
                System.out.println(String.format("[EVALUATED] ID: %s | Title: %s | Match Score: %d%%", job.jobId, job.title, score));

                if (score >= 70) {
                    System.out.println("--> Opening job details page for ID: " + job.jobId);
                    driver.get(job.applyUrl);

                    try {
                        // 1. Check if already applied directly on Naukri page UI
                        List<WebElement> appliedIndicators = driver.findElements(
                            By.xpath("//*[contains(text(),'Already Applied') or contains(text(),'ALREADY APPLIED') or contains(@class,'applied-txt')]")
                        );
                        if (!appliedIndicators.isEmpty() && appliedIndicators.get(0).isDisplayed()) {
                            System.out.println("--> [VERIFIED] Already applied previously on Naukri UI.");
                            DatabaseManager.logAppliedJob(job.jobId, job.company, job.title, score, "APPLIED");
                            continue;
                        }

                        // 2. Locate main Apply button
                        By applyLocator = By.xpath("//button[contains(@class,'apply-button') or contains(text(),'Apply') or contains(text(),'APPLY')] | //div[contains(@class,'apply-button-container')]//button");
                        WebElement applyButton = wait.until(ExpectedConditions.elementToBeClickable(applyLocator));

                        if (applyButton != null && applyButton.isDisplayed()) {
                            String btnText = applyButton.getText().toLowerCase();

                            // Check if external redirect is required
                            if (btnText.contains("company site") || btnText.contains("external")) {
                                System.out.println("--> External redirection required for company portal.");
                                DatabaseManager.logAppliedJob(job.jobId, job.company, job.title, score, "SKIPPED_EXTERNAL");
                                continue;
                            }

                            // Click primary Apply button
                            applyButton.click();
                            System.out.println("--> Primary Apply clicked. Checking for side-pane drawer questions...");
                            Thread.sleep(2000);

                            // 3. Handle side-pane drawer questions if present
                            boolean drawerHandled = DrawerQuestionHandler.handleDrawerQuestions(driver, wait);

                            // 4. Handle secondary confirmation popups or success toasts
                            boolean applicationFinalized = drawerHandled || handleNaukriModalConfirmation(driver, wait);

                            if (applicationFinalized) {
                                System.out.println("--> SUCCESS: Application fully submitted for: " + job.title);
                                DatabaseManager.logAppliedJob(job.jobId, job.company, job.title, score, "APPLIED");
                            } else {
                                System.out.println("--> Application incomplete or required external form.");
                                DatabaseManager.logAppliedJob(job.jobId, job.company, job.title, score, "SKIPPED_EXTERNAL");
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("--> Could not interact with apply elements: " + e.getMessage());
                        DatabaseManager.logAppliedJob(job.jobId, job.company, job.title, score, "SKIPPED_EXTERNAL");
                    }
                } else {
                    System.out.println("--> Skipped due to low match score (" + score + "%).");
                    DatabaseManager.logAppliedJob(job.jobId, job.company, job.title, score, "REJECTED_MATCH_SCORE");
                }

                // Random delay between job applications (human emulation)
                Thread.sleep(2000 + (long)(Math.random() * 1500));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
            DatabaseManager.cleanup();
            System.out.println("\n=== Application Run Complete ===");
        }
    }

    private static boolean handleNaukriModalConfirmation(WebDriver driver, WebDriverWait wait) {
        try {
            // Check for success toast or confirmation banner
            List<WebElement> successMsg = driver.findElements(
                By.xpath("//*[contains(text(),'successfully applied') or contains(text(),'Applied Successfully') or contains(@class,'apply-message')]")
            );
            if (!successMsg.isEmpty()) {
                return true;
            }

            // Check for modal submit/confirm buttons
            By modalConfirmLocator = By.xpath("//button[contains(text(),'Submit') or contains(text(),'Apply') or contains(text(),'Save & Apply')] | //div[contains(@class,'drawer')]//button[contains(@class,'primary')]");
            List<WebElement> modalButtons = driver.findElements(modalConfirmLocator);

            if (!modalButtons.isEmpty() && modalButtons.get(0).isDisplayed()) {
                modalButtons.get(0).click();
                Thread.sleep(2000);
                return true;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
package com.jobagent;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class JobService {

    public static class JobDetails {
        public String jobId;
        public String title;
        public String company;
        public String location;
        public String applyUrl;
        public String postedTimeText;

        @Override
        public String toString() {
            return String.format("ID: %s | Title: %s | Company: %s | Posted: %s", 
                    jobId, title, company, postedTimeText);
        }
    }

    public List<JobDetails> searchJobs(WebDriver driver, String keyword, int maxPages, int maxFreshnessDays) {
        List<JobDetails> jobList = new ArrayList<>();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        for (int page = 1; page <= maxPages; page++) {
            // Appending jobAge filter parameter to search URL (e.g. jobAge=7)
            String baseUrl = "https://www.naukri.com/" + keyword.toLowerCase().replaceAll("\\s+", "-") + "-jobs";
            String searchUrl = (page == 1 ? baseUrl : baseUrl + "-" + page) + "?jobAge=" + maxFreshnessDays;

            System.out.println("\nNavigating to Page " + page + " (Filtered <= " + maxFreshnessDays + " days old): " + searchUrl);
            driver.get(searchUrl);

            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.cust-job-tuple, div.srp-jobtuple-wrapper")));
                List<WebElement> jobCards = driver.findElements(By.cssSelector("div.cust-job-tuple, div.srp-jobtuple-wrapper"));

                System.out.println("Found " + jobCards.size() + " job postings on page " + page + ".");

                for (WebElement card : jobCards) {
                    try {
                        JobDetails job = new JobDetails();
                        
                        // Extract Job ID
                        job.jobId = card.getAttribute("data-job-id");
                        if (job.jobId == null || job.jobId.isEmpty()) continue;

                        // Extract Title & Apply Link
                        WebElement titleElement = card.findElement(By.cssSelector("a.title"));
                        job.title = titleElement.getText().trim();
                        job.applyUrl = titleElement.getAttribute("href");

                        // Extract Company Name
                        WebElement companyElement = card.findElement(By.cssSelector("a.comp-name, a.subTitle"));
                        job.company = companyElement.getText().trim();

                        // Extract Location
                        try {
                            WebElement locElement = card.findElement(By.cssSelector("span.locWraper, span.loc-wrap"));
                            job.location = locElement.getText().trim();
                        } catch (Exception e) {
                            job.location = "Not Specified";
                        }

                        // Extract Posted Date Tag (e.g., "1 Day Ago", "3 Days Ago", "Just Now", "30+ Days Ago")
                        try {
                            WebElement timeElement = card.findElement(By.cssSelector("span.job-post-day, span.type.br2"));
                            job.postedTimeText = timeElement.getText().trim();
                        } catch (Exception e) {
                            job.postedTimeText = "Recently";
                        }

                        // Local check to filter out older postings (> 7 days) if returned by search
                        if (isWithinFreshnessLimit(job.postedTimeText, maxFreshnessDays)) {
                            jobList.add(job);
                        } else {
                            System.out.println("[SKIP - TOO OLD] " + job.title + " (" + job.postedTimeText + ")");
                        }

                    } catch (Exception ignored) {}
                }
            } catch (Exception e) {
                System.err.println("Error parsing jobs on page " + page + ": " + e.getMessage());
            }
        }

        return jobList;
    }

    private boolean isWithinFreshnessLimit(String timeText, int maxDays) {
        if (timeText == null) return true;
        String text = timeText.toLowerCase();

        if (text.contains("just now") || text.contains("today") || text.contains("few hours")) {
            return true;
        }

        if (text.contains("day ago") || text.contains("days ago")) {
            try {
                String numStr = text.replaceAll("[^0-9]", "");
                if (!numStr.isEmpty()) {
                    int days = Integer.parseInt(numStr);
                    return days <= maxDays;
                }
            } catch (Exception ignored) {}
        }

        if (text.contains("30+") || text.contains("month")) {
            return false;
        }

        return true;
    }
}
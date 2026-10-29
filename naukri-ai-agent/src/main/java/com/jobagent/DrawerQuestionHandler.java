package com.jobagent;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class DrawerQuestionHandler {

    /**
     * Detects if Naukri opened a side drawer containing questions, 
     * answers text inputs & radio buttons, and submits the form.
     */
    public static boolean handleDrawerQuestions(WebDriver driver, WebDriverWait wait) {
        try {
            // Locate side-drawer / modal container
            By drawerLocator = By.xpath("//div[contains(@class,'drawer') or contains(@class,'apply-drawer') or contains(@class,'custom-question') or contains(@class,'bot-container')]");
            List<WebElement> drawers = driver.findElements(drawerLocator);

            if (drawers.isEmpty() || !drawers.get(0).isDisplayed()) {
                return false; // No question side-pane present
            }

            WebElement drawer = drawers.get(0);
            System.out.println("--> Detected question side-pane/drawer. Answering questions...");

            // Find all question containers (rows/nodes inside drawer)
            List<WebElement> questionNodes = drawer.findElements(By.xpath(".//div[contains(@class,'question') or contains(@class,'form-node') or contains(@class,'field')] | .//li"));

            for (WebElement node : questionNodes) {
                try {
                    String questionText = node.getText().toLowerCase().trim();
                    if (questionText.isEmpty()) continue;

                    String answer = deriveAnswerForQuestion(questionText);

                    // 1. Handle Text Box / Input / Textarea fields
                    List<WebElement> inputs = node.findElements(By.xpath(".//input[@type='text' or @type='number' or not(@type)] | .//textarea"));
                    if (!inputs.isEmpty() && inputs.get(0).isDisplayed()) {
                        WebElement input = inputs.get(0);
                        if (input.getAttribute("value") == null || input.getAttribute("value").isEmpty()) {
                            input.clear();
                            input.sendKeys(answer);
                            System.out.println("    [TEXT FIELD] Answered '" + questionText.split("\n")[0] + "' -> " + answer);
                        }
                        continue;
                    }

                    // 2. Handle Radio Buttons / Clickable Option Chips
                    List<WebElement> radioOptions = node.findElements(By.xpath(".//label | .//input[@type='radio']/.. | .//span[contains(@class,'option')]"));
                    if (!radioOptions.isEmpty()) {
                        boolean optionSelected = false;
                        for (WebElement option : radioOptions) {
                            String optionText = option.getText().toLowerCase().trim();

                            // Match exact or closest radio option
                            if (shouldSelectRadioOption(answer, optionText)) {
                                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", option);
                                option.click();
                                System.out.println("    [RADIO BUTTON] Selected '" + optionText + "' for question: " + questionText.split("\n")[0]);
                                optionSelected = true;
                                break;
                            }
                        }
                        // Default fallback: click "Yes" or first option if no match found
                        if (!optionSelected && !radioOptions.isEmpty()) {
                            radioOptions.get(0).click();
                        }
                    }

                } catch (Exception ignored) {
                    // Continue answering remaining fields if individual element interaction fails
                }
            }

            // Click the OK / Apply / Save & Apply button in the side pane
            By submitLocator = By.xpath(".//button[contains(text(),'Apply') or contains(text(),'Submit') or contains(text(),'Save & Apply') or contains(text(),'OK')]");
            List<WebElement> submitButtons = drawer.findElements(submitLocator);

            if (!submitButtons.isEmpty() && submitButtons.get(0).isDisplayed()) {
                WebElement submitBtn = submitButtons.get(0);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submitBtn);
                submitBtn.click();
                System.out.println("--> Clicked drawer submit/apply button.");
                Thread.sleep(2500);
                return true;
            }

        } catch (Exception e) {
            System.err.println("--> Drawer handling exception: " + e.getMessage());
        }

        return false;
    }

    /**
     * Map question keywords to your resume / profile answers.
     */
    private static String deriveAnswerForQuestion(String question) {
        if (question.contains("dob") || question.contains("date of birth")) {
            return "24/08/1994";
        }
        if (question.contains("relocate") || question.contains("willing to work")) {
            return "Yes";
        }
        if (question.contains("total") && question.contains("experience")) {
            return "9";
        }
        if (question.contains("gherkin")) {
            return "7";
        }
        if (question.contains("java selenium") || question.contains("automation testing")) {
            return "7";
        }
        if (question.contains("rest assured") || question.contains("api testing")) {
            return "7";
        }
        if (question.contains("bdd")) {
            return "More than 5 years";
        }
        if (question.contains("ci/cd") || question.contains("cicd")) {
            return "3-5 years";
        }
        if (question.contains("notice period")) {
            return "3 months";
        }
        if (question.contains("last working day") || question.contains("lwd")) {
            return "Serving notice period / 30 days";
        }
        if (question.contains("highest qualification")) {
            return "B. Tech, IT & 2016";
        }
        if (question.contains("location")) {
            return "Chennai";
        }
        if (question.contains("infosys") || question.contains("exinfy") || question.contains("ex-infy")) {
            return "No";
        }

        // Default heuristic fallbacks for numeric experience vs yes/no
        if (question.contains("years") || question.contains("experience")) {
            return "7";
        }

        return "Yes";
    }

    private static boolean shouldSelectRadioOption(String answer, String optionText) {
        String ansLower = answer.toLowerCase();
        if (ansLower.equals(optionText)) return true;
        if (ansLower.contains("yes") && optionText.contains("yes")) return true;
        if (ansLower.contains("no") && optionText.contains("no")) return true;
        if (ansLower.contains("more than 5") && (optionText.contains("more than 5") || optionText.contains(">5") || optionText.contains("5+"))) return true;
        if (ansLower.contains("3-5") && optionText.contains("3-5")) return true;
        return false;
    }
}
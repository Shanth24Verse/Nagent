package com.jobagent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/naukri_agent";
    private static final String USER = "root";
    private static final String PASS = "your_password"; // Replace with your MySQL password

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public static boolean isJobAlreadyApplied(String jobId) {
        String query = "SELECT status FROM applied_jobs WHERE job_id = ? AND status IN ('APPLIED', 'REJECTED_MATCH_SCORE')";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, jobId);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Returns true ONLY if already applied or low score match
        } catch (Exception e) {
            return false;
        }
    }

    public static void logAppliedJob(String jobId, String company, String title, int score, String status) {
        String query = "INSERT INTO applied_jobs (job_id, company_name, job_title, match_score, status, applied_date) " +
                       "VALUES (?, ?, ?, ?, ?, NOW()) " +
                       "ON DUPLICATE KEY UPDATE status = VALUES(status), applied_date = NOW()";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, jobId);
            stmt.setString(2, company);
            stmt.setString(3, title);
            stmt.setInt(4, score);
            stmt.setString(5, status);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cleanup() {
        // Optional cleanup hooks
    }
}
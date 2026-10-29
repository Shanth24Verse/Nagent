package com.jobagent;

public class TestDB {
    public static void main(String[] args) {
        System.out.println("Testing MySQL Connection...");
        boolean exists = DatabaseManager.isJobAlreadyApplied("TEST_JOB_001");
        System.out.println("Database Connected Successfully! Job exists check returned: " + exists);
    }
}
# Naukri AI Application Agent 🤖💼

An automated Java-based AI job application agent designed to discover, evaluate, and apply for automation testing positions on Naukri.com with intelligent match scoring and database deduplication.

---

## 🛠️ Prerequisites & Tool Versions

Ensure you have the following tool versions installed and configured on your machine:

| Technology / Tool | Required Version | Purpose |
| :--- | :--- | :--- |
| **Java Development Kit (JDK)** | **Java 21** (Temurin / Oracle) | Core programming language runtime and compiler |
| **Apache Maven** | **3.9.x** (or compatible) | Dependency management and project build tool |
| **MySQL Server** | **8.0+** | Persistent database for application tracking & deduplication |
| **Google Chrome** | **Latest Stable** | Browser instance controlled via Selenium WebDriver |
| **Selenium WebDriver** | **4.28.1+** | Browser automation engine |

---

## ⚙️ Project Architecture & Tech Stack

* **Language**: Java 21
* **Automation**: Selenium WebDriver 4 with Persistent Chrome Session (`BotProfile`)
* **Database**: MySQL with `applied_jobs` tracking and duplicate protection
* **AI Evaluation**: Heuristic/LLM scoring engine targeting $\ge70\%$ match score and 7-day freshness filtering

---

## 🚀 Setup & Installation Guide

### 1. Database Setup
Log into your MySQL instance and create the database schema:
```sql
CREATE DATABASE naukri_agent;
USE naukri_agent;

CREATE TABLE applied_jobs (
    job_id VARCHAR(50) PRIMARY KEY,
    company_name VARCHAR(255),
    job_title VARCHAR(255),
    match_score INT,
    status VARCHAR(50),
    applied_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
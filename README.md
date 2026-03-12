# 💼 Job Application Tracker with AI Interview Coach

A full-stack Java web application built with **Spring Boot** to track job applications and generate AI-powered interview preparation questions using the **Google Gemini API**.

> Built as a personal portfolio project to demonstrate Java backend development, REST API design, MySQL database integration, and real-world AI API integration.

---

## 🚀 Live Features

- ➕ **Add job applications** — company, role, date, status, notes
- 📋 **View all applications** in a clean table with color-coded status badges
- 🔍 **Filter by status** — Applied, Interview, Offer, Rejected
- 🤖 **AI Interview Prep** — click Prepare to generate 3 role-specific interview questions via Gemini AI
- 🗑️ **Delete applications** with confirmation
- 🌙 **Dark / Light mode** toggle with localStorage persistence

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.2.4 |
| REST API | Spring MVC, RESTful endpoints |
| Database | MySQL 8.0, Spring Data JPA, Hibernate |
| AI Integration | Google Gemini 2.0 Flash API |
| Frontend | HTML5, CSS3, Vanilla JavaScript (fetch API) |
| Build Tool | Maven |
| Version Control | Git, GitHub |

---

## 📁 Project Structure

```
src/main/java/com/example/jobapplicationtracker/
├── config/
│   └── WebConfig.java              # CORS configuration
├── controller/
│   ├── JobApplicationController.java
│   └── InterviewQuestionController.java
├── service/
│   ├── JobApplicationService.java
│   ├── InterviewQuestionService.java
│   └── GeminiService.java          # Gemini AI API integration
├── repository/
│   ├── JobApplicationRepository.java
│   └── InterviewQuestionRepository.java
├── model/
│   ├── JobApplication.java
│   └── InterviewQuestion.java
└── exception/
    └── ResourceNotFoundException.java

src/main/resources/
├── static/
│   ├── index.html
│   ├── css/styles.css
│   └── js/app.js
├── application.properties
└── data.sql
```

---

## 🗄️ Database Schema

```sql
-- Table 1: Job Applications
CREATE TABLE job_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    job_role VARCHAR(255) NOT NULL,
    date_applied DATE,
    status VARCHAR(50),
    notes TEXT
);

-- Table 2: AI Generated Interview Questions
CREATE TABLE interview_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    generated_by VARCHAR(100),
    created_at DATETIME,
    FOREIGN KEY (application_id) REFERENCES job_applications(id)
);
```

---

## 🔌 REST API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/applications` | Get all applications |
| `GET` | `/api/applications?status=APPLIED` | Filter by status |
| `POST` | `/api/applications` | Add new application |
| `PUT` | `/api/applications/{id}` | Update status/notes |
| `DELETE` | `/api/applications/{id}` | Delete application |
| `POST` | `/api/applications/{id}/prepare` | Generate AI interview questions |
| `GET` | `/api/applications/{id}/questions` | Get saved questions |

---

## ⚙️ Setup & Installation

### Prerequisites
- Java 21+
- Maven 3.9+
- MySQL 8.0+
- Google Gemini API key (free at [aistudio.google.com](https://aistudio.google.com))

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/mohdazimi83/job-application-tracker.git
cd job-application-tracker
```

**2. Create MySQL database**
```sql
CREATE DATABASE job_tracker_db;
```

**3. Configure application.properties**

Create `src/main/resources/application.properties` (not tracked by Git):
```properties
spring.application.name=job-application-tracker
spring.datasource.url=jdbc:mysql://localhost:3306/job_tracker_db
spring.datasource.username=root
spring.datasource.password=your_mysql_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.sql.init.mode=never
gemini.api.key=your_gemini_api_key
```

**4. Build and run**
```bash
mvn clean
mvn spring-boot:run
```

**5. Open in browser**
```
http://localhost:8080
```

---

## 🔒 Security Notes

- `application.properties` is excluded from Git via `.gitignore` to protect credentials
- Never commit API keys or database passwords to version control

---

## 👨‍💻 Author

**Mohd Azim**
- 📧 mohdazimi083@gmail.com
- 🔗 [LinkedIn](https://linkedin.com/in/AzimI)
- 🐙 [GitHub](https://github.com/mohdazimi83)
- 🏆 [HackerRank](https://hackerrank.com/profile/azimroyal95)

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

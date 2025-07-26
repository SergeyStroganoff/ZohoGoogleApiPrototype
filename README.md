# Google Calendar to Zoho Books Sync

This project is a Java 17 AWS Lambda function that automates syncing events from **Google Calendar** with **Zoho Books**
, including customer deduplication, mileage tracking, and contact creation.

It is deployed using the **AWS SAM CLI** and follows a CI/CD pipeline powered by **GitHub Actions**.

---

## 🚀 Features

- Fetches scheduled Google Calendar events via Google Calendar API
- Checks for duplicate events using DynamoDB
- Automatically creates or updates contacts in Zoho Books via API
- Logs mileage and distance from a configurable office address
- Scheduled to run hourly (configurable)
- Stores secure credentials using AWS Parameter Store
- Logs output to AWS CloudWatch
- Code coverage tracked with JaCoCo

---

## 🛠 Technologies Used

- **Java 17**
- **AWS Lambda** with SAM
- **AWS DynamoDB**
- **AWS Systems Manager Parameter Store**
- **Google Calendar API**
- **Zoho Books API**
- **GitHub Actions**
- **JaCoCo** for test coverage
- **JUnit 5 + Mockito** for unit tests

---

## Continuous Deployment (CI/CD)

- Deployment is automated via GitHub Actions. On each push to main:
- Code is compiled and tested
- JaCoCo report is generated
- Lambda is built and deployed via sam deploy

### Secrets required in GitHub:

- AWS_ACCESS_KEY_ID
- AWS_SECRET_ACCESS_KEY

## 🧪 Local Build and Test

```bash
# Build the project
mvn clean install

# Run tests and generate JaCoCo coverage report
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html


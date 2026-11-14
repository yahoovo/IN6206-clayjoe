# IN6206-clayjoe
# CLAYJOE Handmade Ceramics Store

CLAYJOE is a local single-merchant ceramics store with an Angular frontend, a Spring Boot commerce backend, an independent notification service and two MySQL databases. This guide explains how to restore the data, configure the services and run the application locally. Payments are simulated; no real money is transferred.

## 1. Repository Contents

| Path | Contents |
| --- | --- |
| `backend/` | Commerce backend, Maven wrapper and tests |
| `notification-service/` | Independent notification service and Maven wrapper |
| `clayjoe-frontend/` | Angular frontend, dependency lockfile and static product images |
| `documents/` | Functional and technical documentation |
| `README.md` | Project overview and local setup instructions |

Run commands from the repository root unless another working directory is specified.

### Database Backups and Final Report

The coursework submission package provides the final `CLAYJOE_Report.docx` and two database backups, `clayjoe.sql` and `clayjoe_notifications.sql`. These files are separate submission materials and are not assumed to be included in this repository.

Before following the restoration commands in Section 3, obtain the database backups and place them in a local `database/` folder at the repository root. If that folder is already present, use its backups after checking that they are the intended versions. The optional `manifest.json` contains backup metadata and checksums.

## 2. Prerequisites

| Software | Requirement |
| --- | --- |
| Java | JDK 21 |
| Node.js | Node.js 24.15.0 or later within the 24.x series, compatible with the locked Angular dependencies |
| npm | The frontend declares npm 11.19.1 in `package.json` |
| MySQL | MySQL Server and command-line client, version 8.4 |
| IntelliJ IDEA | Optional; both Java services can also run through their Maven wrappers |

The commerce project declares Spring Boot 4.1.1; the notification project declares Spring Boot 4.1.2. Maven wrappers are included, so a separate Maven installation is not required. Initial Maven dependency resolution and `npm ci` require Internet access.

Confirm the selected local tools:

```bash
java -version
node --version
npm --version
mysql --version
```

## 3. Restore the Databases

Start MySQL and connect using a database administrator account:

```bash
mysql -u root -p
```

Create the databases and application users. Replace the password placeholders with your own local values before executing these statements:

```sql
CREATE DATABASE clayjoe
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE DATABASE clayjoe_notifications
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE USER 'clayjoe_app'@'localhost'
  IDENTIFIED BY 'REPLACE_WITH_COMMERCE_PASSWORD';

CREATE USER 'clayjoe_notify'@'localhost'
  IDENTIFIED BY 'REPLACE_WITH_NOTIFICATION_PASSWORD';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES
  ON clayjoe.* TO 'clayjoe_app'@'localhost';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES
  ON clayjoe_notifications.* TO 'clayjoe_notify'@'localhost';
```

If these databases or users already exist, check their contents and credentials before changing them. The SQL backups contain `DROP TABLE` statements: restoring into an existing database replaces its tables. Use fresh databases or back up existing data first.

Exit the MySQL console and run the following from the project root:

```bash
mysql -u root -p clayjoe < database/clayjoe.sql
mysql -u root -p clayjoe_notifications < database/clayjoe_notifications.sql
```

Use your own administrator username if it differs from `root`. Restoring the supplied backups provides the demonstration data; application startup alone does not recreate the complete catalogue or accounts.

## 4. Configure the Services

The `application.properties` files read passwords and the internal service key from environment variables.

| Service | Environment variable | Value |
| --- | --- | --- |
| Commerce | `CLAYJOE_DB_PASSWORD` | Password for MySQL user `clayjoe_app` |
| Commerce | `CLAYJOE_INTERNAL_API_KEY` | Shared internal service key |
| Notifications | `NOTIFICATION_DB_PASSWORD` | Password for MySQL user `clayjoe_notify` |
| Notifications | `CLAYJOE_INTERNAL_API_KEY` | The same internal service key used by commerce |
| Commerce, optional | `CLAYJOE_SGD_TO_CNY_RATE` | Fixed demonstration exchange rate; defaults to `5.30` |

Choose an internal key of at least 32 characters. The key must match in both Java processes and must never be supplied to the Angular application. Replace all placeholder values below before running the services. Do not commit real passwords or keys.

Default database connections:

| Service | JDBC URL | Database user |
| --- | --- | --- |
| Commerce | `jdbc:mysql://localhost:3306/clayjoe` | `clayjoe_app` |
| Notifications | `jdbc:mysql://localhost:3306/clayjoe_notifications` | `clayjoe_notify` |

For a different MySQL host, port or username, update the local properties or supply Spring Boot overrides such as `SPRING_DATASOURCE_URL` and `SPRING_DATASOURCE_USERNAME` in the appropriate service environment.

## 5. Start the Application

Start services in this order: MySQL, notification service, commerce backend, then frontend. Keep each application running in its own terminal.

### 5.1 Notification Service

From the project root, open a terminal and run:

```bash
cd notification-service
export NOTIFICATION_DB_PASSWORD='REPLACE_WITH_NOTIFICATION_PASSWORD'
export CLAYJOE_INTERNAL_API_KEY='REPLACE_WITH_THE_SAME_SHARED_KEY_AT_LEAST_32_CHARACTERS'
./mvnw spring-boot:run
```

The service listens on `127.0.0.1:8084` and uses `clayjoe_notifications`.

### 5.2 Commerce Backend

From the project root, open another terminal and run:

```bash
cd backend
export CLAYJOE_DB_PASSWORD='REPLACE_WITH_COMMERCE_PASSWORD'
export CLAYJOE_INTERNAL_API_KEY='REPLACE_WITH_THE_SAME_SHARED_KEY_AT_LEAST_32_CHARACTERS'
./mvnw spring-boot:run -Dspring-boot.run.profiles=local-demo
```

The backend listens on port `8083` and uses `clayjoe`. The `local-demo` profile enables simulated payments while `prod` is inactive. Do not activate `prod` for the payment demonstration.

The shell examples use macOS/Linux syntax. If a Maven wrapper lacks execute permission, run `chmod +x mvnw` inside its project directory. On Windows, use `mvnw.cmd` and configure the equivalent environment variables in PowerShell or IDEA.

### 5.3 Angular Frontend

From the project root, open a third terminal and run:

```bash
cd clayjoe-frontend
npm ci
npm start
```

Open **http://localhost:4200/**. Use `localhost` consistently in the browser to avoid session-cookie differences between hostnames.

The development proxy forwards `/api/**` to `http://localhost:8083`. The commerce backend communicates with the notification service; the frontend does not call the internal notification API directly. Product images are included in `public/`.

### 5.4 Running the Java Services in IntelliJ IDEA

Open each Java project using its `pom.xml`, import Maven dependencies and select JDK 21 for both the project and Maven runner.

| Service | Main class | Active profile |
| --- | --- | --- |
| Commerce | `com.clayjoe.backend.BackendApplication` | `local-demo` |
| Notifications | `com.clayjoe.notificationservice.NotificationServiceApplication` | Leave blank |

Add the corresponding environment variables from Section 4 to each run configuration. Database passwords belong in environment variables, not in the Active profiles field. Variables configured in IDEA are not automatically available to a separate terminal or test configuration.

## 6. Create Demonstration Accounts

Restored accounts contain password hashes; this guide does not provide their plaintext passwords. Register a new customer account through the application to obtain known login credentials. Public registration creates a `USER` account.

To create a local demonstration administrator, register a separate account, then use a database administrator connection to promote that specific username:

```sql
UPDATE clayjoe.app_users
SET role = 'ADMIN'
WHERE username = 'your_registered_test_username';
```

Use the stored lowercase username. Sign out and sign in again after the change. Keep separate customer and administrator accounts when checking ownership and role restrictions.

## 7. Check Local Operation

| Check | URL or action |
| --- | --- |
| Storefront | `http://localhost:4200/` |
| Commerce health | `http://localhost:8083/api/health` |
| Notification health | `http://localhost:8084/api/notifications/health` |
| Personal notifications | Sign in, then open `http://localhost:4200/notifications` |

Health responses confirm basic service availability; they do not verify the complete purchase workflow.

Suggested manual check:

1. Register and sign in as a customer.
2. Select CN or SG and add an available product to the cart.
3. Enter delivery details with a shipping country matching the selected market, then place an order.
4. Refresh the message centre and check for the new-order notification.
5. Simulate a successful payment and check the payment notification.
6. Sign in as an administrator and record the carrier and tracking number.
7. Return to the order owner's account, check the shipment notification and confirm receipt.
8. Check bilingual display and verify that another customer cannot access these orders or notifications.

These are verification instructions, not a statement that a fresh installation has already passed all checks. Historical orders do not receive backfilled notifications.

## 8. Tests and Build

Run focused commerce tests from `backend/`:

```bash
./mvnw -Dtest=PricingServiceTest,OrderNotificationServiceTest test
```

Compile the notification service from `notification-service/`:

```bash
./mvnw -DskipTests compile
```

From `clayjoe-frontend/`, run frontend tests and create a production build:

```bash
npm test -- --watch=false
npm run build
```

The focused commerce tests use mocked collaborators. A full Maven test run may also start a Spring application context and require appropriate environment variables and database access. Compiling a service or building the frontend does not establish that all runtime workflows pass.

## 9. Troubleshooting

| Symptom | Check |
| --- | --- |
| MySQL connection refused | Confirm MySQL is running and the configured host and port are correct. |
| MySQL access denied | Check the service's database username, password environment variable and MySQL grants. |
| Missing configuration placeholder | Supply the required environment variable to the process that starts the service. |
| Internal notification API returns 401 | Check that both services use the same internal key. |
| Notification service rejects the key at startup | Use a nonblank key of at least 32 characters. |
| Port already in use | Stop the earlier application instance or adjust ports and corresponding proxy/service URLs consistently. |
| Simulated payment unavailable | Activate `local-demo` on commerce and ensure `prod` is inactive. |
| Signed out after a backend restart | Sign in again; the previous session may no longer be valid. |
| Unsupported Node.js version | Select a Node.js version compatible with the locked frontend dependencies, as specified in Section 2. |
| New notification does not appear | Confirm the notification service is running, the shared key matches, and the relevant commerce transition occurred after startup. Refresh the owner's message centre and inspect service logs. |

## 10. Current Operating Boundaries

- This guide covers local execution. The Angular development proxy and current service bindings are not a public production deployment configuration.
- Payments use local simulation rather than real payment-provider integration.
- Notifications are sent through synchronous HTTP after the commerce transaction commits. Delivery failures do not undo the commerce transaction; there is no durable outbox or automatic retry, so messages can be lost.
- The message centre displays the latest 20 notifications. Viewing a message does not mark it as read.
- The fixed SGD/CNY rate is demonstration configuration, not live foreign-exchange data.
- Before publishing SQL backups to a public GitHub repository, review their contents and remove personal or sensitive records. Keep real credentials and internal keys outside version control.


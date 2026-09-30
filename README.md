# Pet Management System

A Java Swing desktop application for managing pet records through MySQL JDBC. The project demonstrates database connectivity, parameterized SQL, authentication, table-model population, search filtering, and create/read/update/delete operations.

## Technology

- Java SE 8 or later (Swing and JDBC)
- MySQL 8.0 or later
- MySQL Connector/J 8.4.0 or a compatible Connector/J release

## Project Layout

```text
Pet-Management-System/
├── database/
│   └── schema.sql
├── src/
│   └── PetManagementSystem.java
└── README.md
```

## Database Setup

1. Install and start MySQL Server.
2. Run `database/schema.sql` with a MySQL account allowed to create a database and tables. For example, from the project directory:

   ```text
   mysql -u root -p < database/schema.sql
   ```

   The script creates `pet_management_db`, its `users` and `pets` tables, the `admin` login, and sample pet rows. Sample pets are added only when a matching name, species, and breed are not already present, so rerunning the script does not duplicate them.
3. The application connects to `jdbc:mysql://localhost:3306/pet_management_db` as MySQL user `root` with password `root`. If your local MySQL credentials differ, update `DB_USER` and `DB_PASSWORD` near the top of `src/PetManagementSystem.java` before compiling.
4. Sign in with username `admin` and password `admin123`.

## Add the JDBC Driver

Download the MySQL Connector/J platform-independent archive from the official MySQL Connector/J download page and place its JAR in a `lib` directory under the project root. The commands below use `lib\mysql-connector-j-8.4.0.jar`; substitute the actual JAR filename if you downloaded another compatible version.

## Compile and Run on Windows

Run these commands from the `Pet-Management-System` directory in Command Prompt or PowerShell:

```text
mkdir out
javac -cp ".;lib\mysql-connector-j-8.4.0.jar" -d out src\PetManagementSystem.java
java -cp "out;lib\mysql-connector-j-8.4.0.jar" PetManagementSystem
```

The MySQL server must be running before launch. The application loads the pet table after successful login; enter text in the search field to filter by pet name or species. Select a row to load it into the form before updating or deleting it.

## Compile and Run on macOS or Linux

Run these commands from the project directory, using the downloaded Connector/J JAR filename:

```sh
mkdir -p out
javac -cp ".:lib/mysql-connector-j-8.4.0.jar" -d out src/PetManagementSystem.java
java -cp "out:lib/mysql-connector-j-8.4.0.jar" PetManagementSystem
```

## Security Note

This project follows the requested database-lab schema and connection settings. The schema stores passwords as plaintext and the sample JDBC configuration embeds a database password; these are intentionally simple demonstration choices and are not appropriate for a production deployment. A deployed system should use password hashing, externalized secrets, a least-privilege database account, and appropriate transport/database access controls.
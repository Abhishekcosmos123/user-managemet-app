# User Management App

Google App Engine Standard Java application for managing user records with Excel upload, Datastore storage, and BigQuery migration.

## ✨ Key Features & UI Enhancements

This application provides a comprehensive solution for user management, featuring a modern and intuitive user interface:

-   **Modern & Responsive UI**: A clean, Google Cloud-inspired aesthetic with a bright theme, consistent typography, subtle gradients, and elevated card designs.
-   **Login Screen**: Secure authentication for users uploaded via Excel, serving as the application's entry point. Navigation links are disabled when logged out.
-   **Excel Upload**: An intuitive page for administrators to upload user data from Excel files into Google Cloud Datastore.
-   **User Directory**: A visually appealing, paginated table to view, search, filter, and delete user records from Datastore. Supports server-side pagination with configurable page sizes (10, 25, 50, 100).
-   **BigQuery Migration Dashboard**: A dedicated page to review users stored in Datastore and trigger a bulk migration process to a BigQuery `User` table. Now uses efficient **batch loading via Google Cloud Storage** to optimize for free-tier usage and scale.

## 📚 Documentation

**👉 For complete setup instructions, see [SETUP.md](SETUP.md)**

## Quick Start (Local Development)

Follow these steps to get the application running on your local machine:

1.  **Google Cloud Setup:**
    *   Ensure you have a Google Cloud Project configured.
    *   Create a Google Cloud service account with `Datastore Editor`, `BigQuery Data Editor`, `BigQuery User`, and `Storage Admin` roles.
    *   Download the service account key JSON file.
    *   Enable the Datastore, BigQuery, and Cloud Storage APIs for your project.

2.  **Environment Variables (Recommended):**
    Set the following environment variables. Replace `your-project-id` with your GCP project ID and `/absolute/path/to/service-account-key.json` with the actual path to your downloaded JSON key file.
    ```bash
    export GOOGLE_CLOUD_PROJECT=your-project-id
    export GOOGLE_APPLICATION_CREDENTIALS=/absolute/path/to/service-account-key.json
    ```
    Alternatively, you can place the JSON key file in `src/main/resources/` and update `src/main/resources/config.properties` if you prefer (less secure for production).

3.  **Build the Application:**
    Navigate to the project root and build the application using Maven:
    ```bash
    mvn clean package
    ```

4.  **Run Locally (Preferred Method - Jetty Server):**
    This command starts an embedded Jetty server, serving the web application. The application will be accessible at `http://localhost:8080/`.
    ```bash
    mvn jetty:run
    ```

    *Optional (Legacy Mode - Embedded Jetty via ServerMain):*
    ```bash
    mvn exec:java -Dexec.cleanupDaemonThreads=false
    ```

5.  **Access the Application:**
    Once the server is running, open your web browser to:
    -   `http://localhost:8080/` → **Login Page** (first screen)
    -   `http://localhost:8080/upload.html` → **Excel Upload**
    -   `http://localhost:8080/users.html` → **User Directory** (requires login)
    -   `http://localhost:8080/migrate.html` → **BigQuery Migration** (requires login, displays users from Datastore)

## Login Setup

To log in and interact with the application:

1.  **Upload Users via Excel First:**
    *   Navigate to `/upload.html`.
    *   You can use the `/sample/generate` endpoint to download a sample Excel file (`sample-users.xlsx`) with ~100 records for demonstration purposes.
    *   Upload your Excel file containing user data (Name, DOB, Email, Password, Phone, Gender, Address).

2.  **Then, Log In:**
    *   Go to the Login Page at `/`.
    *   Use an email and password from the Excel file you just uploaded.
    *   **Important**: Email is normalized to lowercase for lookup, but the password is case-sensitive.

## Troubleshooting Login

-   **"User not found" error:**
    *   Ensure you have successfully uploaded users via Excel.
    *   Verify the email exists in Datastore (you can check via the User Directory after initial setup or directly in the Google Cloud Console).
-   **"Invalid password" error:**
    *   Remember that passwords are case-sensitive. Double-check your spelling.
-   **Connection or Permissions errors (Datastore/BigQuery):**
    *   Confirm that `GOOGLE_CLOUD_PROJECT` and `GOOGLE_APPLICATION_CREDENTIALS` environment variables are correctly set.
    *   Ensure your service account has the necessary Datastore, BigQuery, and Storage permissions as mentioned in the Google Cloud Setup step.

## Deployment to Google App Engine

For deploying your application to Google App Engine Standard Environment (Java 11/17):

1.  Ensure you are authenticated with `gcloud`:
    ```bash
    gcloud auth login
    gcloud config set project your-project-id
    ```
2.  Deploy the application:
    ```bash
    gcloud app deploy
    ```

See [SETUP.md](SETUP.md) for more detailed deployment instructions and advanced configurations.

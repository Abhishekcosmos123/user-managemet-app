# User Management App (App Engine Standard Java)

This project is a minimal servlet-based App Engine app that ingests user data from Excel, stores it in Google Cloud Datastore, and can migrate data to BigQuery.

Quickstart
1. Set your Google Cloud project:
   - export GOOGLE_CLOUD_PROJECT=your-gcp-project-id
2. If running locally, set Application Default Credentials or set `GOOGLE_APPLICATION_CREDENTIALS` to a service account JSON with Datastore and BigQuery permissions.
3. Build:
   - mvn package
4. Deploy:
   - gcloud app deploy

Notes
- Upload an Excel file with headers: Name, DOB, Email, Password, Phone, Gender, Address
- The app uses email as the Datastore key name for `User` kind.
- Use the BigQuery migration UI to migrate all records to BigQuery.


# User Management App

Google App Engine Standard Java application for managing user records with Excel upload, Datastore storage, and BigQuery migration.

## 📚 Documentation

**👉 For complete setup instructions, see [SETUP.md](SETUP.md)**

## Quick Start

1. **Set environment variables:**
   ```bash
   export GOOGLE_CLOUD_PROJECT=your-project-id
   export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account-key.json
   ```

2. **Build and run:**
   ```bash
   mvn clean package
   mvn exec:java -Dexec.cleanupDaemonThreads=false
   ```

3. **Access:** http://localhost:8080 (Login screen is the first page)

## Features

- ✅ **Login Screen** - First screen, authenticates users from Excel data
- ✅ **Excel Upload** - Upload user data (Name, DOB, Email, Password, Phone, Gender, Address)
- ✅ **User Directory** - View, search, filter, and delete users
- ✅ **BigQuery Migration** - Bulk migrate users from Datastore to BigQuery

## Login Setup

1. **First, upload users via Excel:**
   - Go to `/upload.html` or use `/sample/generate` to download a sample Excel
   - Upload the Excel file with user data

2. **Then login:**
   - Go to `/` (login screen)
   - Use email and password from your uploaded Excel file
   - Email is case-insensitive, password is case-sensitive

## Troubleshooting Login

**"User not found" error:**
- Make sure you've uploaded users via Excel first
- Check email exists in Datastore (via User Directory or Cloud Console)

**"Invalid password" error:**
- Password is case-sensitive - check exact spelling
- Verify password in Excel matches what you're typing

**Connection errors:**
- Verify `GOOGLE_CLOUD_PROJECT` and `GOOGLE_APPLICATION_CREDENTIALS` are set
- Check service account has Datastore permissions

## Deployment

```bash
gcloud app deploy
```

See [SETUP.md](SETUP.md) for detailed deployment instructions.

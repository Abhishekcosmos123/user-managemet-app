# User Management App - Setup & Login Guide

## Prerequisites

1. **Java 11+** installed
2. **Maven 3.6+** installed
3. **Google Cloud Project** with:
   - Cloud Datastore API enabled
   - BigQuery API enabled
   - Service account with Datastore and BigQuery permissions

## Step 1: Google Cloud Setup

### 1.1 Create or Select a GCP Project

```bash
# Install Google Cloud SDK if not already installed
# https://cloud.google.com/sdk/docs/install

# Login to GCP
gcloud auth login

# Create a new project (or use existing)
gcloud projects create user-management-app --name="User Management App"

# Set as active project
gcloud config set project user-management-app
```

### 1.2 Enable Required APIs

```bash
# Enable Datastore API
gcloud services enable datastore.googleapis.com

# Enable BigQuery API
gcloud services enable bigquery.googleapis.com
```

### 1.3 Create Service Account

```bash
# Create service account
gcloud iam service-accounts create user-management-sa \
    --display-name="User Management Service Account"

# Grant Datastore permissions
gcloud projects add-iam-policy-binding user-management-app \
    --member="serviceAccount:user-management-sa@user-management-app.iam.gserviceaccount.com" \
    --role="roles/datastore.user"

# Grant BigQuery permissions
gcloud projects add-iam-policy-binding user-management-app \
    --member="serviceAccount:user-management-sa@user-management-app.iam.gserviceaccount.com" \
    --role="roles/bigquery.dataEditor"

gcloud projects add-iam-policy-binding user-management-app \
    --member="serviceAccount:user-management-sa@user-management-app.iam.gserviceaccount.com" \
    --role="roles/bigquery.jobUser"
```

### 1.4 Download Service Account Key

```bash
# Create and download JSON key
gcloud iam service-accounts keys create ~/user-management-key.json \
    --iam-account=user-management-sa@user-management-app.iam.gserviceaccount.com
```

## Step 2: Local Environment Setup

### 2.1 Clone/Download the Project

```bash
cd /Users/a1/Desktop/user-management-app
```

### 2.2 Set Environment Variables

Create a `.env` file in the project root (or export in your shell):

```bash
# .env file
export GOOGLE_CLOUD_PROJECT=user-management-app
export GOOGLE_APPLICATION_CREDENTIALS=~/user-management-key.json
```

Or export directly in your shell:

```bash
export GOOGLE_CLOUD_PROJECT=user-management-app
export GOOGLE_APPLICATION_CREDENTIALS=~/user-management-key.json
```

**For Windows (PowerShell):**
```powershell
$env:GOOGLE_CLOUD_PROJECT="user-management-app"
$env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\user-management-key.json"
```

### 2.3 Verify Datastore Setup

```bash
# Initialize Datastore (if using Datastore mode, not Firestore)
# Note: For App Engine Standard, Datastore is automatically configured
# For local testing, you may need to use the Datastore emulator or connect to a real project

# Start Datastore emulator (optional, for local testing)
gcloud beta emulators datastore start --host-port=localhost:8081
```

## Step 3: Build and Run

### 3.1 Build the Project

```bash
# Clean and build
mvn clean package

# If you encounter dependency issues, force update:
mvn -U clean package
```

### 3.2 Run Locally

**Option A: Using Maven Exec Plugin (Recommended)**
```bash
mvn exec:java -Dexec.cleanupDaemonThreads=false
```

**Option B: Using Shaded JAR**
```bash
java -jar target/user-management-app-0.1.0.jar
```

**Option C: Using Jetty Maven Plugin**
```bash
mvn jetty:run
```

The application will start on `http://localhost:8080`

## Step 4: Initial Setup - Upload Users

### 4.1 Generate Sample Excel File

1. Open browser: `http://localhost:8080`
2. You'll see the **Login screen** (this is the first screen)
3. Navigate to: `http://localhost:8080/sample/generate` to download a sample Excel file with ~100 users

### 4.2 Upload Users via Excel

1. Go to: `http://localhost:8080/upload.html` (or click "Upload" in navbar after login)
2. Click "Download sample" to get a sample Excel file, OR
3. Create your own Excel file with these columns:
   - **Name** (e.g., "John Doe")
   - **DOB** (e.g., "1990-01-15" or date format)
   - **Email** (e.g., "john.doe@example.com")
   - **Password** (e.g., "Password123")
   - **Phone** (e.g., "+1234567890")
   - **Gender** (e.g., "Male", "Female", "Other")
   - **Address** (e.g., "123 Main St, City, State")

4. Upload the Excel file (.xlsx format)
5. You should see: `{"imported": 100}` or similar success message

### 4.3 Verify Users in Datastore

You can check via:
- **User Directory**: `http://localhost:8080/users.html` (requires login)
- **Google Cloud Console**: Datastore → Entities → Kind: "User"

## Step 5: Login Process

### 5.1 Login Screen

1. Open: `http://localhost:8080/` (Login is the first screen)
2. Enter credentials from your uploaded Excel file:
   - **Email**: Use the exact email from the Excel file (case-insensitive)
   - **Password**: Use the exact password from the Excel file (case-sensitive)

### 5.2 Common Login Issues & Solutions

**Issue: "Invalid email or password. User not found."**
- **Solution**: Make sure you've uploaded users via Excel first
- Check that the email exists in Datastore (via User Directory or Cloud Console)
- Email is case-insensitive, but must match exactly (without extra spaces)

**Issue: "Invalid email or password."**
- **Solution**: Password is case-sensitive - check exact spelling
- Verify the password in your Excel file matches what you're typing
- Check for extra spaces or special characters

**Issue: "Login error: ..."**
- **Solution**: Check `GOOGLE_APPLICATION_CREDENTIALS` is set correctly
- Verify service account has Datastore permissions
- Check application logs for detailed error messages

### 5.3 Successful Login

After successful login:
- You'll see: "Login successful! Redirecting..."
- Automatically redirected to: `http://localhost:8080/users.html`
- Session lasts 30 minutes

## Step 6: Using the Application

### 6.1 User Directory

- **URL**: `http://localhost:8080/users.html`
- **Features**:
  - View all users in a table
  - Search by name, email, or phone
  - Delete users
  - Refresh to reload data

### 6.2 BigQuery Migration

- **URL**: `http://localhost:8080/migrate.html`
- **Steps**:
  1. Enter BigQuery dataset name (default: `user_dataset`)
  2. Enter table name (default: `User`)
  3. Click "Migrate All Users"
  4. Wait for confirmation message

**Note**: Ensure your service account has BigQuery permissions and the dataset exists (or will be created automatically).

## Step 7: Deploy to App Engine

### 7.1 Prepare for Deployment

```bash
# Set your GCP project
gcloud config set project user-management-app

# Verify app.yaml exists
cat app.yaml
```

### 7.2 Deploy

```bash
# Deploy to App Engine Standard
gcloud app deploy

# Follow prompts to select region, etc.
```

### 7.3 Access Deployed App

```bash
# Get the app URL
gcloud app browse

# Or visit: https://user-management-app.uc.r.appspot.com
# (URL will vary based on your project)
```

## Troubleshooting

### Build Issues

**Maven dependency errors:**
```bash
# Clear local repository cache
rm -rf ~/.m2/repository/com/google/cloud
mvn -U clean package
```

**Java version mismatch:**
```bash
# Verify Java version
java -version  # Should be 11+

# Set JAVA_HOME if needed
export JAVA_HOME=/path/to/java11
```

### Runtime Issues

**Datastore connection errors:**
- Verify `GOOGLE_CLOUD_PROJECT` is set
- Verify `GOOGLE_APPLICATION_CREDENTIALS` points to valid JSON key
- Check service account has `roles/datastore.user` permission

**BigQuery migration errors:**
- Verify BigQuery API is enabled
- Check service account has BigQuery permissions
- Ensure dataset exists or can be created

**Login not working:**
- Verify users exist in Datastore (check via Cloud Console)
- Check email/password match exactly (password is case-sensitive)
- Review application logs for errors

### Debug Mode

Enable debug logging by checking application logs:

```bash
# For local run, check console output
# For App Engine, check logs:
gcloud app logs tail -s default
```

## Quick Reference

### Environment Variables
```bash
GOOGLE_CLOUD_PROJECT=your-project-id
GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account-key.json
```

### Key URLs (Local)
- Login: `http://localhost:8080/`
- Upload: `http://localhost:8080/upload.html`
- Users: `http://localhost:8080/users.html`
- Migrate: `http://localhost:8080/migrate.html`
- Sample Excel: `http://localhost:8080/sample/generate`

### Excel File Format
Required columns (case-insensitive headers):
- Name
- DOB
- Email
- Password
- Phone
- Gender
- Address

## Support

For issues:
1. Check application logs
2. Verify GCP permissions
3. Ensure environment variables are set correctly
4. Test with sample Excel file first

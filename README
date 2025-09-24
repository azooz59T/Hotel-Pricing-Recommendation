# AWS S3 Setup for Hotel ETL Project

## Step 1: Create S3 Buckets

1. **Login to AWS Console**
   - Go to https://aws.amazon.com/console/
   - Login with your credentials

2. **Navigate to S3**
   - Search for "S3" in the services search bar
   - Click on S3

3. **Create Input Bucket**
   - Click "Create bucket"
   - Bucket name: `hotel-etl-input-[your-name]` (must be globally unique)
   - Region: Choose closest to you (e.g., us-east-1)
   - Leave other settings as default
   - Click "Create bucket"

4. **Create Output Bucket**
   - Click "Create bucket" again
   - Bucket name: `hotel-etl-output-[your-name]`
   - Same region as input bucket
   - Click "Create bucket"

5. **Create Scripts Bucket**
   - Click "Create bucket"
   - Bucket name: `hotel-etl-scripts-[your-name]`
   - Same region
   - Click "Create bucket"

## Step 2: Upload CSV Files

1. **Open your input bucket** (`hotel-etl-input-[your-name]`)
2. **Click "Upload"**
3. **Add your 4 CSV files:**
   - products.csv
   - bookings.csv
   - buildings.csv
   - prices.csv
4. **Click "Upload"**

## Step 3: Upload ETL Script

1. **Open your scripts bucket** (`hotel-etl-scripts-[your-name]`)
2. **Click "Upload"**
3. **Upload your `simple_data_ingestion.py` file**
4. **Click "Upload"**

## Step 4: Set Up IAM Role for Glue

1. **Go to IAM service** in AWS Console
2. **Click "Roles"**
3. **Click "Create role"**
4. **Select "AWS service"**
5. **Choose "Glue"**
6. **Click "Next"**
7. **Attach these policies:**
   - `AWSGlueServiceRole`
   - `AmazonS3FullAccess` (for simplicity in testing)
8. **Role name:** `GlueETLRole`
9. **Click "Create role"**

## Your S3 URLs will be:
- Input: `s3://hotel-etl-input-[your-name]/`
- Output: `s3://hotel-etl-output-[your-name]/`
- Script: `s3://hotel-etl-scripts-[your-name]/simple_data_ingestion.py`

# AWS Glue Job Setup

## Step 1: Navigate to AWS Glue

1. **Go to AWS Glue** in the console
2. **Click "Jobs"** in the left sidebar
3. **Click "Create job"**

## Step 2: Configure Job

1. **Job details:**
   - Name: `hotel-etl-job`
   - Description: `ETL job for hotel pricing data`

2. **IAM Role:**
   - Select the `GlueETLRole` you created

3. **Type:** 
   - Select "Spark"

4. **Glue version:**
   - Select "Glue 3.0" (recommended)

5. **Language:**
   - Python 3

6. **Script:**
   - Choose "Upload and edit an existing script"
   - Script path: `s3://hotel-etl-scripts-[your-name]/simple_data_ingestion.py`

## Step 3: Job Parameters

Add these job parameters (click "Advanced properties"):

| Key | Value |
|-----|-------|
| `--input_path` | `s3://hotel-etl-input-[your-name]` |
| `--output_path` | `s3://hotel-etl-output-[your-name]` |

## Step 4: Resource Configuration

- **Worker type:** G.1X (smallest for testing)
- **Number of workers:** 2 (minimum)
- **Job timeout:** 10 minutes

## Step 5: Save and Run

1. **Click "Save"**
2. **Click "Run job"** to test it

## Monitoring Your Job

1. **Job runs** tab shows execution history
2. **Logs** tab shows detailed output
3. **CloudWatch logs** for detailed debugging

## Expected Results

After successful run, check your output bucket:
- `s3://hotel-etl-output-[your-name]/products/`
- `s3://hotel-etl-output-[your-name]/bookings/`
- `s3://hotel-etl-output-[your-name]/buildings/`
- `s3://hotel-etl-output-[your-name]/prices/`

Each folder will contain Parquet files with your transformed data.
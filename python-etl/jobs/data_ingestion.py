# Hotel Pricing ETL Job for AWS Glue

import sys
from awsglue.transforms import *
from awsglue.utils import getResolvedOptions
from pyspark.context import SparkContext
from awsglue.context import GlueContext
from awsglue.job import Job
from awsglue.dynamicframe import DynamicFrame
from pyspark.sql.functions import col, current_timestamp
from pyspark.sql.types import StructType, StructField, StringType, IntegerType, DecimalType

def get_table_schema(table_name):
    """Simple schema definitions"""
    
    schemas = {
        'products': StructType([
            StructField("Id", StringType(), False),
            StructField("Room Name", StringType(), True),
            StructField("Arrival Date", StringType(), True),
            StructField("No. of Beds", IntegerType(), True),
            StructField("Room Type", StringType(), True),
            StructField("Grade", IntegerType(), True),
            StructField("Private Pool", StringType(), True)
        ]),
        
        'bookings': StructType([
            StructField("Id", StringType(), False),
            StructField("Product Id", StringType(), True),
            StructField("Creation Date", StringType(), True),
            StructField("Confirmation Status", StringType(), True),
            StructField("Arrival Date", StringType(), True)
        ]),
        
        'buildings': StructType([
            StructField("Building", StringType(), True),
            StructField("Product Id", StringType(), True)
        ]),
        
        'prices': StructType([
            StructField("Product Id", StringType(), True),
            StructField("Price", DecimalType(10, 2), True),
            StructField("Currency", StringType(), True)
        ])
    }
    
    return schemas.get(table_name)

def read_csv_file(glue_context, file_path, table_name):
    """Read CSV file from S3 using Glue DynamicFrame with simple schema"""
    try:
        schema = get_table_schema(table_name)
        
        dynamic_frame = glue_context.create_dynamic_frame.from_options(
            connection_type="s3",
            connection_options={"paths": [file_path]},
            format="csv",
            format_options={"withHeader": True, "separator": ","}
        )
        
        df = dynamic_frame.toDF()
        
        # Apply schema if defined
        if schema:
            df = glue_context.spark_session.createDataFrame(df.rdd, schema)
        
        df = df.withColumn("processed_at", current_timestamp())
        
        return df
        
    except Exception as e:
        return glue_context.spark_session.createDataFrame([], schema=None)

def clean_data(df, table_name):
    """Apply basic data cleaning transformations"""
    if df.count() == 0:
        return df
    
    try:
        if table_name == "products":
            df = df.withColumn("Private Pool", col("Private Pool").cast("string"))
        elif table_name == "prices":
            df = df.filter(col("Price") > 0)
        
        return df
    except Exception:
        return df

def save_as_parquet(glue_context, df, output_path, table_name):
    """Save DataFrame as Parquet to S3"""
    if df.count() == 0:
        return
    
    try:
        full_path = f"{output_path}/{table_name}/"
        dynamic_frame = DynamicFrame.fromDF(df, glue_context, table_name)
        
        glue_context.write_dynamic_frame.from_options(
            frame=dynamic_frame,
            connection_type="s3",
            connection_options={"path": full_path},
            format="parquet"
        )
        
    except Exception:
        pass

def process_hotel_data(glue_context, input_base_path, output_base_path):
    """Main ETL processing function"""
    tables = ["products", "bookings", "buildings", "prices"]
    
    for table in tables:
        try:
            file_path = f"{input_base_path}/{table}.csv"
            df = read_csv_file(glue_context, file_path, table)
            
            if df.count() > 0:
                df_clean = clean_data(df, table)
                save_as_parquet(glue_context, df_clean, output_base_path, table)
                
        except Exception:
            continue

def main():
    """Main function for AWS Glue job"""
    args = getResolvedOptions(sys.argv, ['JOB_NAME', 'input_path', 'output_path'])
    
    sc = SparkContext()
    glueContext = GlueContext(sc)
    job = Job(glueContext)
    job.init(args['JOB_NAME'], args)
    
    try:
        process_hotel_data(
            glue_context=glueContext,
            input_base_path=args['input_path'],
            output_base_path=args['output_path']
        )
        
    except Exception:
        raise
    
    finally:
        job.commit()

if __name__ == "__main__":
    main()
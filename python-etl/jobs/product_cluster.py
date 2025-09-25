# product_cluster.py
# Complete clustering with booking analysis

import sys
from awsglue.transforms import *
from awsglue.utils import getResolvedOptions
from pyspark.context import SparkContext
from awsglue.context import GlueContext
from awsglue.job import Job
from awsglue.dynamicframe import DynamicFrame
from pyspark.sql.functions import col, concat_ws, date_format, count, current_timestamp, lit, sum as spark_sum, avg, when

def read_parquet_data(glue_context, file_path):
    """Read parquet file from S3"""
    try:
        df = glue_context.spark_session.read.parquet(file_path)
        return df
    except Exception as e:
        return None

def create_product_clusters(products_df):
    """Create clusters for products based on attributes"""
    try:
        clustered_df = products_df.withColumn(
            "cluster_key",
            concat_ws("_",
                date_format(col("arrival_date"), "MMM-yyyy"),
                col("room_type"),
                concat_ws("-", col("`no._of_beds`").cast("string"), lit("beds")),
                concat_ws("-", col("Grade").cast("string"), lit("stars")),
                col("private_pool")
            )
        )
        return clustered_df
    except Exception as e:
        return None

def generate_cluster_summary(clustered_df):
    """Generate summary statistics for clusters"""
    try:
        cluster_summary = clustered_df.groupBy("cluster_key") \
            .agg(count("Id").alias("product_count")) \
            .orderBy(col("product_count").desc())
        return cluster_summary
    except Exception as e:
        return None

def join_products_with_bookings(clustered_df, bookings_df):
    """Join clustered products with booking data"""
    try:
        # Create aliases to avoid ambiguous references
        clustered_products = clustered_df.alias("products")
        bookings_aliased = bookings_df.alias("bookings")

        # Join products with bookings
        products_with_bookings = clustered_products.join(
            bookings_aliased,
            (col("products.Id") == col("bookings.product_id")) &
            (col("products.arrival_date") == col("bookings.arrival_date")),
            "left"  # Left join to keep all products, even those without bookings
        )
        return products_with_bookings
    except Exception as e:
        return None

def calculate_cluster_metrics(products_with_bookings):
    """Calculate performance metrics for each cluster"""
    try:
        cluster_metrics = products_with_bookings.groupBy(col("products.cluster_key")) \
            .agg(
                count(col("products.Id")).alias("total_product_instances"),
                count(when(col("bookings.confirmation_status").isNotNull(), 1)).alias("total_bookings"),
                count(when(col("bookings.confirmation_status") == "Confirmed", 1)).alias("confirmed_bookings"),
                count(when(col("bookings.confirmation_status") == "Pending", 1)).alias("pending_bookings"),
                count(when(col("bookings.confirmation_status") == "Cancelled", 1)).alias("cancelled_bookings")
            ) \
            .withColumn(
                "booking_rate", 
                (col("total_bookings") / col("total_product_instances")).cast("decimal(5,3)")
            ) \
            .withColumn(
                "confirmation_rate",
                when(col("total_bookings") > 0, 
                     (col("confirmed_bookings") / col("total_bookings")).cast("decimal(5,3)")
                ).otherwise(0)
            ) \
            .orderBy(col("booking_rate").desc())
        return cluster_metrics
    except Exception as e:
        return None

def create_pricing_insights(cluster_metrics):
    """Generate pricing recommendations based on cluster performance"""
    try:
        pricing_insights = cluster_metrics.withColumn(
            "pricing_recommendation",
            when(col("booking_rate") >= 0.8, "HIGH_DEMAND - Consider increasing prices")
            .when(col("booking_rate") >= 0.5, "MODERATE_DEMAND - Monitor and adjust")
            .when(col("booking_rate") >= 0.2, "LOW_DEMAND - Consider promotions")
            .otherwise("VERY_LOW_DEMAND - Review pricing strategy")
        )
        return pricing_insights
    except Exception as e:
        return None

def save_dataframe_as_parquet(glue_context, df, output_path, table_name):
    """Save DataFrame as Parquet to S3"""
    if df is None or df.count() == 0:
        return False
    
    try:
        full_path = f"{output_path}/{table_name}"
        # Force single file output using coalesce(1)
        dynamic_frame = DynamicFrame.fromDF(df.coalesce(1), glue_context, table_name)
        
        glue_context.write_dynamic_frame.from_options(
            frame=dynamic_frame,
            connection_type="s3",
            connection_options={"path": full_path},
            format="parquet"
        )
        return True
    except Exception as e:
        return False

def process_clustering_analysis(glue_context, input_path, output_path):
    """Main clustering processing function"""
    
    # Read input data
    products_path = f"{input_path}/products"
    bookings_path = f"{input_path}/bookings"
    
    products_df = read_parquet_data(glue_context, products_path)
    if products_df is None:
        return False
    
    bookings_df = read_parquet_data(glue_context, bookings_path)
    if bookings_df is None:
        return False
    
    product_count = products_df.count()
    
    if product_count == 0:
        return False
    
    # Step 1: Create clusters
    clustered_df = create_product_clusters(products_df)
    if clustered_df is None:
        return False
    
    # Step 2: Generate cluster summary
    cluster_summary = generate_cluster_summary(clustered_df)
    if cluster_summary is None:
        return False
    
    # Step 3: Join with bookings
    products_with_bookings = join_products_with_bookings(clustered_df, bookings_df)
    if products_with_bookings is None:
        return False
    
    # Step 4: Calculate metrics
    cluster_metrics = calculate_cluster_metrics(products_with_bookings)
    if cluster_metrics is None:
        return False
    
    # Step 5: Create pricing insights
    pricing_insights = create_pricing_insights(cluster_metrics)
    if pricing_insights is None:
        return False
    
    # Step 6: Save all results
    results = {
        "clustered_products": clustered_df,
        "cluster_summary": cluster_summary,
        "cluster_metrics": cluster_metrics,
        "pricing_insights": pricing_insights
    }
    
    success_count = 0
    for table_name, df in results.items():
        if save_dataframe_as_parquet(glue_context, df, output_path, table_name):
            success_count += 1
    
    return success_count == len(results)

def main():
    """Main function for AWS Glue job"""
    args = getResolvedOptions(sys.argv, ['JOB_NAME', 'input_path', 'output_path'])
    
    sc = SparkContext()
    glue_context = GlueContext(sc)
    job = Job(glue_context)
    job.init(args['JOB_NAME'], args)
    
    try:
        success = process_clustering_analysis(
            glue_context=glue_context,
            input_path=args['input_path'],
            output_path=args['output_path']
        )
        
        if not success:
            raise Exception("Job completed with errors")
        
    except Exception as e:
        raise
    
    finally:
        job.commit()

if __name__ == "__main__":
    main()
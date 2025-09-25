import pandas as pd
import random
from datetime import datetime, timedelta
import uuid

# Set random seed for reproducible data
random.seed(42)

def generate_products_data(num_records=1000):
    """Generate Products CSV data"""
    room_types = ['Single', 'Double Room', 'Suite', 'Deluxe', 'Standard']
    room_names = ['Room A', 'Room B', 'Room C', 'Executive Suite', 'Presidential Suite', 
                  'Ocean View', 'Garden View', 'City View', 'Penthouse', 'Standard Room']
    grades = [1, 2, 3, 4, 5, 7]  # Star ratings
    
    products = []
    base_date = datetime(2024, 1, 1)
    
    for i in range(num_records):
        arrival_date = base_date + timedelta(days=random.randint(0, 365))
        products.append({
            'Id': f'PROD_{i+1:04d}',
            'Room Name': random.choice(room_names),
            'Arrival Date': arrival_date.strftime('%Y-%m-%d'),
            'No. of Beds': random.choice([1, 2, 3, 4, 6]),
            'Room Type': random.choice(room_types),
            'Grade': random.choice(grades),
            'Private Pool': random.choice(['Yes', 'No'])
        })
    
    return pd.DataFrame(products)

def generate_bookings_data(product_ids, num_records=5000):
    """Generate Bookings CSV data"""
    statuses = ['Confirmed', 'Pending', 'Cancelled']
    
    bookings = []
    base_date = datetime(2024, 1, 1)
    
    for i in range(num_records):
        creation_date = base_date + timedelta(days=random.randint(0, 300))
        arrival_date = creation_date + timedelta(days=random.randint(1, 60))
        
        bookings.append({
            'Id': f'BOOK_{i+1:06d}',
            'Product Id': random.choice(product_ids),
            'Creation Date': creation_date.strftime('%Y-%m-%d'),
            'Confirmation Status': random.choice(statuses),
            'Arrival Date': arrival_date.strftime('%Y-%m-%d')
        })
    
    return pd.DataFrame(bookings)

def generate_buildings_data(product_ids):
    """Generate Buildings CSV data"""
    buildings = ['Building 1', 'Building 2', 'Building 3', 'North Tower', 'South Tower', 
                'Main Building', 'Annex A', 'Annex B']
    
    building_data = []
    for product_id in product_ids:
        building_data.append({
            'Building': random.choice(buildings),
            'Product Id': product_id
        })
    
    return pd.DataFrame(building_data)

def generate_prices_data(product_ids):
    """Generate Prices CSV data"""
    currencies = ['USD', 'EUR', 'GBP', 'EGP']
    
    prices = []
    for product_id in product_ids:
        # Each product can have prices in multiple currencies
        num_currencies = random.randint(1, 3)
        selected_currencies = random.sample(currencies, num_currencies)
        
        for currency in selected_currencies:
            base_price = random.randint(50, 500)
            prices.append({
                'Product Id': product_id,
                'Price': base_price,
                'Currency': currency
            })
    
    return pd.DataFrame(prices)

# Generate all datasets
if __name__ == "__main__":
    # Generate products first
    products_df = generate_products_data(100000)
    product_ids = products_df['Id'].tolist()
    
    # Generate other datasets based on product IDs
    bookings_df = generate_bookings_data(product_ids, 1000000)
    buildings_df = generate_buildings_data(product_ids)
    prices_df = generate_prices_data(product_ids)
    
    # Save to CSV files
    products_df.to_csv('products.csv', index=False)
    bookings_df.to_csv('bookings.csv', index=False)
    buildings_df.to_csv('buildings.csv', index=False)
    prices_df.to_csv('prices.csv', index=False)
    
    print("Sample data files generated successfully!")
    print(f"Products: {len(products_df)} records")
    print(f"Bookings: {len(bookings_df)} records")
    print(f"Buildings: {len(buildings_df)} records")
    print(f"Prices: {len(prices_df)} records")
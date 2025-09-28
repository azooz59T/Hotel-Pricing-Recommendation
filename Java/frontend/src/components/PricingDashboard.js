// frontend/src/components/PricingDashboard.js
import React, { useState } from 'react';
import { RefreshCw } from 'lucide-react';
import { useProducts } from '../hooks/useProducts';
import DashboardHeader from './DashboardHeader';
import FiltersSidebar from './FiltersSidebar';
import BuildingCard from './BuildingCard';
import './PricingDashboard.css';

const PricingDashboard = () => {
  const [userRole, setUserRole] = useState('pricing_manager');
  const { products, availableFilters, loading, error, loadData, applyFilters } = useProducts(userRole);

  const getTotalProducts = () => {
    return products.reduce((total, building) => total + building.products.length, 0);
  };

  if (loading) {
    return (
      <div className="dashboard-container">
        <div className="loading-container">
          <RefreshCw className="loading-spinner" />
          <p>Loading pricing data...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="dashboard-container">
        <div className="error-container">
          <h2>Error</h2>
          <p>{error}</p>
          <button onClick={loadData} className="retry-button">
            <RefreshCw /> Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      <DashboardHeader 
        userRole={userRole} 
        onRoleChange={setUserRole} 
        onRefresh={loadData} 
      />

      <div className="dashboard-content">
        <FiltersSidebar 
          availableFilters={availableFilters} 
          onApplyFilters={applyFilters} 
        />

        <div className="products-main">
          <div className="products-header">
            <h2>Products by Building ({getTotalProducts()} total)</h2>
            <p>Role: {userRole.replace('_', ' ')}</p>
          </div>

          <div className="buildings-list">
            {products.map(building => (
              <BuildingCard 
                key={building.buildingName} 
                building={building} 
              />
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default PricingDashboard;
// frontend/src/components/PricingDashboard.js
import React, { useState } from 'react';
import { RefreshCw } from 'lucide-react';
import { useProducts } from '../hooks/useProducts';
import DashboardHeader from './DashboardHeader';
import FiltersSidebar from './FiltersSidebar';
import BuildingCard from './BuildingCard';
import MultiCurrencyTable from './MultiCurrencyTable';
import './PricingDashboard.css';

const PricingDashboard = () => {
  const [userRole, setUserRole] = useState('pricing_manager');
  const [activeTab, setActiveTab] = useState('buildings');
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

      {/* Tab Navigation */}
      <div className="tab-navigation">
        <button 
          className={`tab-button ${activeTab === 'buildings' ? 'active' : ''}`}
          onClick={() => setActiveTab('buildings')}
        >
          Products by Building
        </button>
        <button 
          className={`tab-button ${activeTab === 'currency' ? 'active' : ''}`}
          onClick={() => setActiveTab('currency')}
        >
          Multi-Currency View
        </button>
      </div>

      <div className="dashboard-content">
        {activeTab === 'buildings' ? (
          <>
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
          </>
        ) : (
          <div className="full-width-content">
            <MultiCurrencyTable />
          </div>
        )}
      </div>
    </div>
  );
};

export default PricingDashboard;
// frontend/src/components/DashboardHeader.js
import React from 'react';
import { RefreshCw } from 'lucide-react';

const DashboardHeader = ({ userRole, onRoleChange, onRefresh }) => {
  const getRoleDescription = (role) => {
    switch (role) {
      case 'pricing_manager':
        return "Manage room pricing recommendations with comprehensive filtering";
      case 'regional_manager':
        return "Monitor regional performance and building metrics";
      case 'reporting_user':
        return "Access data for analysis and reporting";
      default:
        return "";
    }
  };

  return (
    <div className="dashboard-header">
      <div className="header-content">
        <div>
          <h1>Hotel Pricing Dashboard</h1>
          <p>{getRoleDescription(userRole)}</p>
        </div>
        <div className="header-controls">
          <div>
            <label>User Role:</label>
            <select
              value={userRole}
              onChange={(e) => onRoleChange(e.target.value)}
              className="role-select"
            >
              <option value="pricing_manager">Pricing Manager</option>
              <option value="regional_manager">Regional Manager</option>
              <option value="reporting_user">Reporting User</option>
            </select>
          </div>
          <button onClick={onRefresh} className="refresh-button">
            <RefreshCw />
            Refresh
          </button>
        </div>
      </div>
    </div>
  );
};

export default DashboardHeader;
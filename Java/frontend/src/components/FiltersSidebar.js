// frontend/src/components/FiltersSidebar.js
import React, { useState } from 'react';

const FiltersSidebar = ({ availableFilters, onApplyFilters }) => {
  const [selectedFilters, setSelectedFilters] = useState({});

  const handleFilterChange = (filter, value) => {
    if (value.trim()) {
      const values = value.split(',').map(v => v.trim()).filter(v => v);
      setSelectedFilters(prev => ({
        ...prev,
        [filter]: values
      }));
    } else {
      setSelectedFilters(prev => {
        const newFilters = { ...prev };
        delete newFilters[filter];
        return newFilters;
      });
    }
  };

  const handleApplyFilters = () => {
    onApplyFilters(selectedFilters);
  };

  const getActiveFiltersCount = () => {
    return Object.values(selectedFilters).reduce((count, filterValues) => count + filterValues.length, 0);
  };

  const clearFilters = () => {
    setSelectedFilters({});
    onApplyFilters({});
  };

  return (
    <div className="filters-sidebar">
      <div className="filters-header">
        <h3>Available Filters</h3>
        <span className="filter-count">({availableFilters.length})</span>
        {getActiveFiltersCount() > 0 && (
          <button onClick={clearFilters} className="clear-filters-button">
            Clear
          </button>
        )}
      </div>
      
      <div className="filters-list">
        {availableFilters.map(filter => (
          <div key={filter} className="filter-item">
            <label>{filter.replace('_', ' ').toUpperCase()}</label>
            <input
              type="text"
              placeholder={`Filter by ${filter}`}
              onChange={(e) => handleFilterChange(filter, e.target.value)}
              className="filter-input"
            />
            <small>Enter values separated by commas</small>
          </div>
        ))}
      </div>

      <button onClick={handleApplyFilters} className="apply-filters-button">
        Apply Filters
        {getActiveFiltersCount() > 0 && (
          <span className="filter-badge">({getActiveFiltersCount()})</span>
        )}
      </button>
    </div>
  );
};

export default FiltersSidebar;
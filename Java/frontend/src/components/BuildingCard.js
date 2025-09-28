// frontend/src/components/BuildingCard.js
import React, { useState } from 'react';
import { Building2, ChevronDown, ChevronUp } from 'lucide-react';
import ProductTable from './ProductTable';

const BuildingCard = ({ building }) => {
  const [isExpanded, setIsExpanded] = useState(false);

  const toggleExpanded = () => {
    setIsExpanded(!isExpanded);
  };

  return (
    <div className="building-card">
      <button onClick={toggleExpanded} className="building-header">
        <div className="building-info">
          <Building2 />
          <h3>{building.buildingName}</h3>
          <span className="product-count">
            {building.products.length} rooms
          </span>
        </div>
        {isExpanded ? <ChevronUp /> : <ChevronDown />}
      </button>

      {isExpanded && <ProductTable products={building.products} />}
    </div>
  );
};

export default BuildingCard;
import React from 'react';
import { ChevronUp, ChevronDown, RefreshCw } from 'lucide-react';
import { useMultiCurrency } from '../hooks/useMultiCurrency';

const MultiCurrencyTable = () => {
  const {
    products,
    loading,
    error,
    sortBy,
    sortDirection,
    page,
    pageSize,
    handleSort,
    nextPage,
    previousPage,
    getAllCurrencies,
    loadData
  } = useMultiCurrency();

  const SortHeader = ({ column, children }) => (
    <th 
      onClick={() => handleSort(column)}
      className="sortable-header"
      style={{ cursor: 'pointer', userSelect: 'none' }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
        {children}
        {sortBy === column && (
          sortDirection === 'asc' ? <ChevronUp size={16} /> : <ChevronDown size={16} />
        )}
      </div>
    </th>
  );

  if (loading) {
    return (
      <div className="multi-currency-container">
        <div className="loading-container">
          <RefreshCw className="loading-spinner" />
          <p>Loading multi-currency data...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="multi-currency-container">
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

  const currencies = getAllCurrencies();

  return (
    <div className="multi-currency-container">
      <div className="multi-currency-header">
        <h2>Multi-Currency Price View</h2>
        <p>Showing {products.length} products across {currencies.length} currencies</p>
      </div>

      <div className="table-container">
        <table className="multi-currency-table">
          <thead>
            <tr>
              <th>Product</th>
              <th>Building</th>
              {currencies.map(currency => (
                <SortHeader key={currency} column={currency}>
                  {currency}
                </SortHeader>
              ))}
            </tr>
          </thead>
          <tbody>
            {products.map(product => (
              <tr key={product.productId}>
                <td>
                  <div>
                    <div className="room-name">{product.roomName}</div>
                    <div className="product-id">ID: {product.productId}</div>
                  </div>
                </td>
                <td>{product.buildingName}</td>
                {currencies.map(currency => (
                  <td key={currency} className="price-cell">
                    {product.prices[currency] ? (
                      <span className="price-value">
                        {Number(product.prices[currency]).toFixed(0)}
                      </span>
                    ) : (
                      <span className="no-price">—</span>
                    )}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="pagination-controls">
        <button 
          onClick={previousPage}
          disabled={page === 0}
          className="pagination-button"
        >
          Previous
        </button>
        <span className="pagination-info">Page {page + 1}</span>
        <button 
          onClick={nextPage}
          disabled={products.length < pageSize}
          className="pagination-button"
        >
          Next
        </button>
      </div>
    </div>
  );
};

export default MultiCurrencyTable;
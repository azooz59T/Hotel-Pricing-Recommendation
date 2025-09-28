// frontend/src/components/ProductTable.js
import React from 'react';

const ProductTable = ({ products }) => {
  return (
    <div className="products-table">
      <table>
        <thead>
          <tr>
            <th>Room</th>
            <th>Type</th>
            <th>Features</th>
            <th>Current Price</th>
            <th>Recommended</th>
            <th>Change</th>
          </tr>
        </thead>
        <tbody>
          {products.map(product => {
            const priceChange = ((product.recommendedPrice - product.currentPrice) / product.currentPrice * 100);
            const isIncrease = priceChange > 0;
            
            return (
              <tr key={`${product.productId}-${product.currency}`}>
                <td>
                  <div>
                    <div className="room-name">{product.roomName}</div>
                    <div className="product-id">ID: {product.productId}</div>
                  </div>
                </td>
                <td>
                  {product.roomType} ({product.beds} bed{product.beds !== 1 ? 's' : ''})
                </td>
                <td>
                  {product.privatePool === 'Yes' && (
                    <span className="feature-tag">Pool</span>
                  )}
                </td>
                <td className="price">
                  {product.currentPrice} {product.currency}
                </td>
                <td className="price recommended">
                  {product.recommendedPrice} {product.currency}
                </td>
                <td>
                  <span className={`change-indicator ${isIncrease ? 'increase' : 'decrease'}`}>
                    {isIncrease ? '↗' : '↘'} {Math.abs(priceChange).toFixed(1)}%
                  </span>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};

export default ProductTable;
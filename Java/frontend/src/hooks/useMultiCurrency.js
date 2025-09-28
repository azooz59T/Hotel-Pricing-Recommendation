import { useState, useEffect } from 'react';
import { productAPI } from '../services/api';

export const useMultiCurrency = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [sortBy, setSortBy] = useState('current_price');
  const [sortDirection, setSortDirection] = useState('asc');
  const [page, setPage] = useState(0);
  const [pageSize] = useState(20);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await productAPI.getMultiCurrencyProducts(sortBy, sortDirection, page, pageSize);
      setProducts(response.data);
    } catch (err) {
      setError('Failed to load multi-currency data. Make sure your backend is running on port 8080.');
      console.error('Error loading multi-currency data:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSort = (newSortBy) => {
    if (sortBy === newSortBy) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(newSortBy);
      setSortDirection('asc');
    }
  };

  const nextPage = () => {
    setPage(prev => prev + 1);
  };

  const previousPage = () => {
    setPage(prev => Math.max(0, prev - 1));
  };

  const getAllCurrencies = () => {
    const currencies = new Set();
    products.forEach(product => {
      Object.keys(product.prices || {}).forEach(currency => currencies.add(currency));
    });
    return Array.from(currencies).sort();
  };

  useEffect(() => {
    loadData();
  }, [sortBy, sortDirection, page]);

  return {
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
  };
};
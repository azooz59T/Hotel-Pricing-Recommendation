// frontend/src/hooks/useProducts.js
import { useState, useEffect } from 'react';
import { productAPI } from '../services/api';

export const useProducts = (userRole) => {
  const [products, setProducts] = useState([]);
  const [availableFilters, setAvailableFilters] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      // Load filters for the current role
      const filtersResponse = await productAPI.getFilters(userRole);
      setAvailableFilters(filtersResponse.data);

      // Load products
      const productsResponse = await productAPI.getAllProducts();
      setProducts(productsResponse.data);
    } catch (err) {
      setError('Failed to load data. Make sure your backend is running on port 8080.');
      console.error('Error loading data:', err);
    } finally {
      setLoading(false);
    }
  };

  const applyFilters = async (selectedFilters) => {
    if (Object.keys(selectedFilters).length === 0) {
      loadData();
      return;
    }

    setLoading(true);
    try {
      // Convert selectedFilters to the format your backend expects
      const filterRequest = {};
      
      Object.keys(selectedFilters).forEach(filterKey => {
        switch (filterKey) {
          case 'building':
            filterRequest.buildings = selectedFilters[filterKey];
            break;
          case 'room_type':
            filterRequest.roomTypes = selectedFilters[filterKey];
            break;
          case 'beds':
            filterRequest.beds = selectedFilters[filterKey].map(Number);
            break;
          case 'grade':
            filterRequest.grades = selectedFilters[filterKey].map(Number);
            break;
          case 'private_pool':
            filterRequest.privatePool = selectedFilters[filterKey];
            break;
          case 'currency':
            filterRequest.currency = selectedFilters[filterKey];
            break;
        }
      });

      console.log('Sending filter request:', filterRequest);
      const response = await productAPI.getFilteredProducts(filterRequest);
      setProducts(response.data);
    } catch (err) {
      setError('Failed to apply filters');
      console.error('Error applying filters:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [userRole]);

  return {
    products,
    availableFilters,
    loading,
    error,
    loadData,
    applyFilters
  };
};
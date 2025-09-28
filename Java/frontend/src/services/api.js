import axios from 'axios';

// Base URL
const API_BASE_URL = 'http://localhost:8080/api/products';

const api = axios.create({
  baseURL: 'http://localhost:8080/api/products',
});

export const productAPI = {
  getAllProducts: () => api.get('/grouped-by-building'),
  getFilteredProducts: (filters) => api.post('/grouped-by-building', filters),
  getFilters: (userRole) => api.get(`/filters?userRole=${userRole}`),
  getMultiCurrencyProducts: (sortBy = 'current_price', sortDirection = 'asc', page = 0, size = 20) =>
    api.get(`/multi-currency?sortBy=${sortBy}&sortDirection=${sortDirection}&page=${page}&size=${size}`),
};

export default api;
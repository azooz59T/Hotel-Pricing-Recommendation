// frontend/src/services/api.js
import axios from 'axios';

// Base URL - update this to match your Spring Boot server
const API_BASE_URL = 'http://localhost:8080/api/products';

const api = axios.create({
  baseURL: 'http://localhost:8080/api/products',
});

export const productAPI = {
  getAllProducts: () => api.get('/grouped-by-building'),
  getFilteredProducts: (filters) => api.post('/grouped-by-building', filters),
  getFilters: (userRole) => api.get(`/filters?userRole=${userRole}`),
};

export default api;
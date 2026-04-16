import apiClient from './apiClient';

export const getSuppliers = async () => {
  const { data } = await apiClient.get('/suppliers');
  return data;
};

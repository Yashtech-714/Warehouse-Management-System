import apiClient from './apiClient';

export const getStorageLocations = async () => {
  const { data } = await apiClient.get('/storage-locations');
  return data;
};

export const createStorageLocation = async (payload) => {
  const { data } = await apiClient.post('/storage-locations', payload);
  return data;
};

export const updateStorageLocation = async (id, payload) => {
  const { data } = await apiClient.put(`/storage-locations/${id}`, payload);
  return data;
};

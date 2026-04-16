import apiClient from './apiClient';

export const getInventory = async () => {
  const { data } = await apiClient.get('/inventory');
  return data;
};

export const checkAvailability = async (productId, requestedQuantity) => {
  const { data } = await apiClient.get('/inventory/check', {
    params: { productId, requestedQuantity }
  });
  return data;
};

export const assignToLocation = async (inventoryItemId, storageLocationId) => {
  const { data } = await apiClient.post('/inventory/assign', { inventoryItemId, storageLocationId });
  return data;
};

export const moveBetweenLocations = async (inventoryItemId, targetLocationId) => {
  const { data } = await apiClient.post('/inventory/move', { inventoryItemId, storageLocationId: targetLocationId });
  return data;
};

export const getProducts = async () => {
  const { data } = await apiClient.get('/products');
  return data;
};

export const createProduct = async (payload) => {
  const { data } = await apiClient.post('/products', payload);
  return data;
};

export const updateProduct = async (id, payload) => {
  const { data } = await apiClient.put(`/products/${id}`, payload);
  return data;
};

export const deactivateProduct = async (id) => {
  const { data } = await apiClient.patch(`/products/${id}/deactivate`);
  return data;
};

import apiClient from './apiClient';

export const getPurchaseOrders = async () => {
	const { data } = await apiClient.get('/purchase-orders');
	return data;
};

export const createPurchaseOrder = async (payload) => {
	const { data } = await apiClient.post('/purchase-orders', payload);
	return data;
};

export const sendPurchaseOrder = async (id) => {
	const { data } = await apiClient.post(`/purchase-orders/send/${id}`);
	return data;
};

/** Supplier marks PO as DELIVERED */
export const deliverPurchaseOrder = async (id) => {
  const { data } = await apiClient.patch(`/purchase-orders/${id}/deliver`);
  return data;
};

/** Staff receives stock from a DELIVERED PO into a storage location */
export const receivePurchaseOrder = async (id, storageLocationId) => {
  const { data } = await apiClient.post(`/purchase-orders/${id}/receive`, { storageLocationId });
  return data;
};

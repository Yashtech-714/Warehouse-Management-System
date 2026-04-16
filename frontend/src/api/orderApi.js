import apiClient from './apiClient';

export const getOrders = async () => {
	const { data } = await apiClient.get('/orders');
	return data;
};

export const createOrder = async (payload) => {
	const { data } = await apiClient.post('/orders', payload);
	return data;
};

export const processOrder = async (id) => {
	const { data } = await apiClient.post(`/orders/process/${id}`);
	return data;
};

export const cancelOrder = async (id) => {
	const { data } = await apiClient.delete(`/orders/${id}`);
	return data;
};

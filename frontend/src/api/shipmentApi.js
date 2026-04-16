import apiClient from './apiClient';

export const getShipments = async () => {
  const { data } = await apiClient.get('/shipments');
  return data;
};

// NOTE: createShipment removed — shipments are created automatically
// by the backend when Staff processes an order (POST /orders/process/{id}).

export const trackShipment = async (id) => {
  const { data } = await apiClient.get(`/shipments/${id}`);
  return data;
};

/** STAFF: CREATED → SHIPPED (also syncs linked order to SHIPPED) */
export const markShipmentShipped = async (id) => {
  const { data } = await apiClient.patch(`/shipments/${id}/ship`);
  return data;
};

/** STAFF: SHIPPED → DELIVERED (also syncs linked order to DELIVERED) */
export const markShipmentDelivered = async (id) => {
  const { data } = await apiClient.patch(`/shipments/${id}/deliver`);
  return data;
};

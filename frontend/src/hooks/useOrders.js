import { useCallback, useEffect, useState } from 'react';
import { getOrders } from '../api/orderApi';
import { getApiErrorMessage } from '../services/apiError';

export default function useOrders() {
	const [orders, setOrders] = useState([]);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState('');

	const refresh = useCallback(async () => {
		setLoading(true);
		setError('');
		try {
			const data = await getOrders();
			setOrders(data ?? []);
		} catch (err) {
			setError(getApiErrorMessage(err));
		} finally {
			setLoading(false);
		}
	}, []);

	useEffect(() => {
		refresh();
	}, [refresh]);

	return { orders, loading, error, refresh, setError };
}

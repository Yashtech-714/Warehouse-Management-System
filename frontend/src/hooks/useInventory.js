import { useCallback, useEffect, useState } from 'react';
import { getInventory, getProducts } from '../api/inventoryApi';
import { getApiErrorMessage } from '../services/apiError';

export default function useInventory() {
	const [inventory, setInventory] = useState([]);
	const [products, setProducts] = useState([]);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState('');

	const refresh = useCallback(async () => {
		setLoading(true);
		setError('');
		try {
			const [inventoryData, productsData] = await Promise.all([getInventory(), getProducts()]);
			setInventory(inventoryData ?? []);
			setProducts(productsData ?? []);
		} catch (err) {
			setError(getApiErrorMessage(err));
		} finally {
			setLoading(false);
		}
	}, []);

	useEffect(() => {
		refresh();
	}, [refresh]);

	return { inventory, products, loading, error, refresh, setError };
}

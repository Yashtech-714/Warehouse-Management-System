import { useEffect, useState } from 'react';
import ErrorNotice from '../components/ErrorNotice';
import { getProducts, getInventory } from '../api/inventoryApi';
import { getOrders } from '../api/orderApi';
import { getPurchaseOrders } from '../api/purchaseOrderApi';
import { getShipments } from '../api/shipmentApi';
import { getApiErrorMessage } from '../services/apiError';

function Dashboard() {
	const [stats, setStats] = useState({
		products: 0,
		inventoryItems: 0,
		orders: 0,
		purchaseOrders: 0,
		shipments: 0
	});
	const [error, setError] = useState('');

	useEffect(() => {
		const load = async () => {
			try {
				setError('');
				const [products, inventory, orders, purchaseOrders, shipments] = await Promise.all([
					getProducts(),
					getInventory(),
					getOrders(),
					getPurchaseOrders(),
					getShipments()
				]);

				setStats({
					products: products.length,
					inventoryItems: inventory.length,
					orders: orders.length,
					purchaseOrders: purchaseOrders.length,
					shipments: shipments.length
				});
			} catch (err) {
				setError(getApiErrorMessage(err));
			}
		};

		load();
	}, []);

	return (
		<div className="stack">
			<ErrorNotice message={error} />
			<div className="card-grid">
				<article className="card metric">
					<h3>Products</h3>
					<p>{stats.products}</p>
				</article>
				<article className="card metric">
					<h3>Inventory Items</h3>
					<p>{stats.inventoryItems}</p>
				</article>
				<article className="card metric">
					<h3>Orders</h3>
					<p>{stats.orders}</p>
				</article>
				<article className="card metric">
					<h3>Purchase Orders</h3>
					<p>{stats.purchaseOrders}</p>
				</article>
				<article className="card metric">
					<h3>Shipments</h3>
					<p>{stats.shipments}</p>
				</article>
			</div>
		</div>
	);
}

export default Dashboard;

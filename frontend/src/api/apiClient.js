import axios from 'axios';

const apiClient = axios.create({
	baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
	headers: {
		'Content-Type': 'application/json'
	}
});

// Attach X-User-Role header on every request so backend can validate role-based access
apiClient.interceptors.request.use((config) => {
	try {
		const stored = localStorage.getItem('wms_user');
		if (stored) {
			const user = JSON.parse(stored);
			if (user?.role) config.headers['X-User-Role'] = user.role;
		}
	} catch { /* ignore */ }
	return config;
});

export default apiClient;

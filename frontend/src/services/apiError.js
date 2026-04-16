export function getApiErrorMessage(error) {
	const message = error?.response?.data?.message;
	if (message && typeof message === 'string') {
		return message;
	}

	if (error?.message) {
		return error.message;
	}

	return 'Unexpected error occurred.';
}

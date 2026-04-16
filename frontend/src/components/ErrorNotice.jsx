function ErrorNotice({ message }) {
	if (!message) {
		return null;
	}

	return <div className="error-notice">{message}</div>;
}

export default ErrorNotice;

function Form({ title, fields, values, onChange, onSubmit, submitLabel = 'Save' }) {
	return (
		<section className="card">
			<h3>{title}</h3>
			<form className="form-grid" onSubmit={onSubmit}>
				{fields.map((field) => (
					<label key={field.name} className="field">
						<span>{field.label}</span>
						<input
							type={field.type ?? 'text'}
							name={field.name}
							value={values[field.name] ?? ''}
							onChange={onChange}
							required={field.required ?? true}
							min={field.min}
							step={field.step}
						/>
					</label>
				))}
				<button className="btn" type="submit">
					{submitLabel}
				</button>
			</form>
		</section>
	);
}

export default Form;

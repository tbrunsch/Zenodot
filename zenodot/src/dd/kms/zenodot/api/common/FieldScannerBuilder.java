package dd.kms.zenodot.api.common;

/**
 * Scans the fields of a class considering three optional built-in filters.
 */
public interface FieldScannerBuilder
{
	static FieldScannerBuilder create() {
		return new dd.kms.zenodot.impl.common.FieldScannerBuilderImpl();
	}

	FieldScannerBuilder minimumAccessModifier(AccessModifier minimumAccessModifier);

	FieldScannerBuilder staticMode(StaticMode staticMode);

	FieldScannerBuilder ignoreShadowedFields(boolean ignoreShadowedFields);

	FieldScannerBuilder name(String name);

	FieldScanner build();
}

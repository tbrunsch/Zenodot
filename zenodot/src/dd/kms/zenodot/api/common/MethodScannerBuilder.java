package dd.kms.zenodot.api.common;

/**
 * Scans the methods of a class with three optional built-in filters.
 */
public interface MethodScannerBuilder
{
	static MethodScannerBuilder create() {
		return new dd.kms.zenodot.impl.common.MethodScannerBuilderImpl();
	}

	MethodScannerBuilder minimumAccessModifier(AccessModifier minimumAccessModifier);

	MethodScannerBuilder staticMode(StaticMode staticMode);

	MethodScannerBuilder name(String name);

	MethodScanner build();
}

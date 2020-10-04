package dd.kms.zenodot.api.common;

/**
 * Scans the constructors of a class optionally considering a minimum access modifier.
 */
public interface ConstructorScannerBuilder
{
	static ConstructorScannerBuilder create() {
		return new dd.kms.zenodot.impl.common.ConstructorScannerBuilderImpl();
	}

	ConstructorScannerBuilder minimumAccessModifier(AccessModifier minimumAccessModifier);

	ConstructorScanner build();
}

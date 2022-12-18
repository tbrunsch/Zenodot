package dd.kms.zenodot.api;

import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.ParserSettings;

import java.util.Comparator;

/**
 * API entry point of Zenodot
 */
public class Parsers
{
	public static final Comparator<CodeCompletion> COMPLETION_COMPARATOR	= Comparator.comparing(CodeCompletion::getRating).thenComparing(CodeCompletion::getType);

	/**
	 * This method to loads and initializes Zenodot classes whose initializers may require some time.
	 * It is not necessary to call this method, but it can save some time when these classes are required later.
	 */
	public static void preloadClasses() {
		try {
			Class.forName(dd.kms.zenodot.impl.utils.dataproviders.ClassDataProvider.class.getName(), true, Parsers.class.getClassLoader());
		} catch (ClassNotFoundException ignored) {
		}
	}

	public static ExpressionParser createExpressionParser(ParserSettings settings) {
		return createExpressionParser(settings, Variables.create());
	}

	public static ExpressionParser createExpressionParser(ParserSettings settings, Variables variables) {
		if (variables instanceof dd.kms.zenodot.impl.VariablesImpl) {
			return new dd.kms.zenodot.impl.ExpressionParserImpl(settings, (dd.kms.zenodot.impl.VariablesImpl) variables);
		}
		if (variables == null) {
			throw new IllegalArgumentException("Parameter 'variables' is null");
		}
		throw new IllegalArgumentException("Parameter 'variables' is of a custom type '" + variables.getClass() + "'. "
			+ "This is not supported. Use Variables.create() or Variables.of() to create supported instances of Variables");
	}

	public static ClassParser createClassParser(ParserSettings settings) {
		return new dd.kms.zenodot.impl.ClassParserImpl(settings);
	}

	public static PackageParser createPackageParser(ParserSettings settings) {
		return new dd.kms.zenodot.impl.PackageParserImpl(settings);
	}
}

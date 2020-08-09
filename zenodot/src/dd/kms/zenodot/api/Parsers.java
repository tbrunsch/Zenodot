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

	public static ExpressionParser createExpressionParser(ParserSettings settings) {
		return new dd.kms.zenodot.impl.ExpressionParserImpl(settings);
	}

	public static ClassParser createClassParser(ParserSettings settings) {
		return new dd.kms.zenodot.impl.ClassParserImpl(settings);
	}

	public static PackageParser createPackageParser(ParserSettings settings) {
		return new dd.kms.zenodot.impl.PackageParserImpl(settings);
	}
}

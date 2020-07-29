package dd.kms.zenodot.api;

import dd.kms.zenodot.api.settings.ParserSettings;

/**
 * API entry point of Zenodot
 */
public class Parsers
{
	public static ExpressionParser createExpressionParser(String text, ParserSettings settings) {
		return new dd.kms.zenodot.impl.ExpressionParserImpl(text, settings);
	}

	public static ClassParser createClassParser(String text, ParserSettings settings) {
		return new dd.kms.zenodot.impl.ClassParserImpl(text, settings);
	}

	public static PackageParser createPackageParser(String text, ParserSettings settings) {
		return new dd.kms.zenodot.impl.PackageParserImpl(text, settings);
	}
}

package dd.kms.zenodot.api;

import dd.kms.zenodot.api.settings.ParserSettings;

/**
 * API entry point of Zenodot
 */
public class Parsers
{
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

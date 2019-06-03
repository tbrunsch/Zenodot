package dd.kms.zenodot;

import dd.kms.zenodot.settings.ParserSettings;

/**
 * API entry point of Zenodot
 */
public class Parsers
{
	public static ExpressionParser createExpressionParser(String text, ParserSettings settings, Object thisValue) {
		return new ExpressionParserImpl(text, settings, thisValue);
	}

	public static ClassParser createClassParser(String text, ParserSettings settings) {
		return new ClassParserImpl(text, settings);
	}

	public static PackageParser createPackageParser(String text, ParserSettings settings) {
		return new PackageParserImpl(text, settings);
	}
}

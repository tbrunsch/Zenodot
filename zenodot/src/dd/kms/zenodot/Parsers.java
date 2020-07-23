package dd.kms.zenodot;

import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

/**
 * API entry point of Zenodot
 */
public class Parsers
{
	public static ExpressionParser createExpressionParser(String text, ParserSettings settings) {
		return new ExpressionParserImpl(text, settings);
	}

	public static ClassParser createClassParser(String text, ParserSettings settings) {
		return new ClassParserImpl(text, settings);
	}

	public static PackageParser createPackageParser(String text, ParserSettings settings) {
		return new PackageParserImpl(text, settings);
	}
}

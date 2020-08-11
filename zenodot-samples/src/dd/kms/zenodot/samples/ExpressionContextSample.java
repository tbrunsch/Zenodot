package dd.kms.zenodot.samples;

import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsUtils;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

/**
 * This sample demonstrates how specifying an context influences how expressions are evaluated.
 */
public class ExpressionContextSample
{
	public static void main(String[] args) throws ParseException {
		ParserSettings settings = ParserSettingsUtils.createBuilder().build();
		String expression = "substring(4)";
		ObjectInfo thisValue = InfoProvider.createObjectInfo("Zenodot");
		ExpressionParser parser = Parsers.createExpressionParser(settings);
		System.out.println("Result: " + parser.evaluate(expression, thisValue).getObject());
	}
}

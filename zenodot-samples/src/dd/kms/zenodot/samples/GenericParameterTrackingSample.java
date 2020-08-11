package dd.kms.zenodot.samples;

import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsUtils;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This sample shows that Zenodot tries to track parameters of generic types.
 * If Zenodot would suggest completions only based on the runtime type, then
 * due to type erasure it would have no information about the actual parameter.
 */
public class GenericParameterTrackingSample
{
	public static void main(String[] args) throws ParseException {
		TestClass testInstance = new TestClass();

		ParserSettings settings = ParserSettingsUtils.createBuilder().build();
		ExpressionParser parser = Parsers.createExpressionParser(settings);
		String text = "list.get(0).le";
		ObjectInfo thisValue = InfoProvider.createObjectInfo(testInstance);
		List<CodeCompletion> completions = new ArrayList<>(parser.getCompletions(text, text.length(), thisValue));
		Collections.sort(completions, Parsers.COMPLETION_COMPARATOR);

		System.out.println(completions.get(0).getTextToInsert());
	}

	private static class TestClass
	{
		public final List<String> list = Arrays.asList("This", "is", "a", "list", "of", "strings", ".");
	}
}

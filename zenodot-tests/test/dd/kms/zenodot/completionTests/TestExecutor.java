package dd.kms.zenodot.completionTests;

import dd.kms.zenodot.JavaParser;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.common.AbstractTestExecutor;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.debug.ParserLogEntry;
import dd.kms.zenodot.debug.ParserLoggerIF;
import dd.kms.zenodot.result.CompletionSuggestionIF;
import dd.kms.zenodot.settings.ParserSettings;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Class for creating tests with expected successful code completions
 */
public class TestExecutor extends AbstractTestExecutor<TestExecutor>
{
	public TestExecutor(Object testInstance) {
		super(testInstance);
	}

	public TestExecutor test(String javaExpression, String... expectedSuggestions) {
		ParserLoggerIF logger = prepareLogger(false, -1);

		boolean repeatTestAtError = isStopAtError() || isPrintLogEntriesAtError();
		if (!runTest(javaExpression, !repeatTestAtError, expectedSuggestions) && repeatTestAtError) {
			int numLoggedEntries = logger.getNumberOfLoggedEntries();
			prepareLogger(isPrintLogEntriesAtError(), isStopAtError() ? numLoggedEntries : -1);
			runTest(javaExpression, true, expectedSuggestions);
		}

		return this;
	}

	private boolean runTest(String javaExpression, boolean executeAssertions, String... expectedSuggestions) {
		ParserSettings settings = settingsBuilder.build();
		ParserLoggerIF logger = settings.getLogger();

		logger.log(new ParserLogEntry(LogLevel.INFO, "Test", "Testing expression '" + javaExpression + "'...\n"));

		JavaParser parser = new JavaParser();
		int caret = javaExpression.length();
		List<String> suggestions = null;
		try {
			suggestions = extractSuggestions(parser.suggestCodeCompletion(javaExpression, caret, settings, testInstance));
		} catch (ParseException e) {
			if (executeAssertions) {
				fail("Exception during code completion: " + e.getMessage());
			}
			return false;
		}
		if (executeAssertions) {
			assertTrue(MessageFormat.format("Expression: {0}, expected completions: {1}, actual completions: {2}",
					javaExpression,
					expectedSuggestions,
					suggestions),
					suggestions.size() >= expectedSuggestions.length);
		}
		if (suggestions.size() < expectedSuggestions.length) {
			return false;
		}

		for (int i = 0; i < expectedSuggestions.length; i++) {
			if (executeAssertions) {
				assertEquals("Expression: " + javaExpression, expectedSuggestions[i], suggestions.get(i));
			}
			if (!Objects.equals(expectedSuggestions[i], suggestions.get(i))) {
				return false;
			}
		}
		return true;
	}

	private static List<String> extractSuggestions(List<CompletionSuggestionIF> completions) {
		return completions.stream()
				.map(completion -> completion.getTextToInsert())
				.collect(Collectors.toList());
	}
}
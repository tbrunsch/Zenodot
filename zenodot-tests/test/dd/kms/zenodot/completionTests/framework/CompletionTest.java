package dd.kms.zenodot.completionTests.framework;

import dd.kms.zenodot.JavaParser;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.common.AbstractTest;
import dd.kms.zenodot.debug.ParserLoggerIF;
import dd.kms.zenodot.result.CompletionSuggestionIF;
import dd.kms.zenodot.settings.ParserSettings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public abstract class CompletionTest extends AbstractTest<CompletionTest>
{
	private final TestExecutor	testExecutor;

	protected CompletionTest(TestData testData) {
		super(testData.getTestInstance());
		TestConfigurator testConfigurator = testData.getConfigureSettingsFunction();
		if (testConfigurator != null) {
			testConfigurator.configure(this);
		}
		this.testExecutor = testData.getTestExecutor();
	}

	@Test
	public void testCompletion() {
		testExecutor.executeTest(this);
	}

	void testCompletion(String javaExpression, String[] expectedSuggestions) {
		ParserLoggerIF logger = prepareLogger(false, -1);

		boolean repeatTestAtError = isStopAtError() || isPrintLogEntriesAtError();
		if (!runTest(javaExpression, !repeatTestAtError, expectedSuggestions) && repeatTestAtError) {
			int numLoggedEntries = logger.getNumberOfLoggedEntries();
			prepareLogger(isPrintLogEntriesAtError(), isStopAtError() ? numLoggedEntries : -1);
			runTest(javaExpression, true, expectedSuggestions);
		}
	}

	void testCompletionWithError(String javaExpression, int caret, Class<? extends Exception> expectedExceptionClass) {
		ParserSettings settings = settingsBuilder.build();

		JavaParser parser = new JavaParser();
		try {
			parser.suggestCodeCompletion(javaExpression, caret, settings, testInstance);
			fail("Expression: " + javaExpression + " - Expected an exception");
		} catch (ParseException | IllegalStateException e) {
			assertEquals(expectedExceptionClass, e.getClass());
		}
	}

	private boolean runTest(String javaExpression, boolean executeAssertions, String... expectedSuggestions) {
		ParserSettings settings = settingsBuilder.build();
		ParserLoggerIF logger = settings.getLogger();

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
			assertTrue(MessageFormat.format("expected completions: {1}, actual completions: {2}",
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
				assertEquals(expectedSuggestions[i], suggestions.get(i));
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

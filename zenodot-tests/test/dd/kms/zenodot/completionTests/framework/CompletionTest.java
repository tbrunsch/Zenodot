package dd.kms.zenodot.completionTests.framework;

import dd.kms.zenodot.JavaParser;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.common.AbstractTest;
import dd.kms.zenodot.debug.ParserLogger;
import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.CompletionSuggestions;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionField;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionVariable;
import dd.kms.zenodot.settings.ParserSettings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.MessageFormat;
import java.util.*;
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
		ParserLogger logger = prepareLogger(false, -1);

		boolean repeatTestAtError = isStopAtError() || isPrintLogEntriesAtError();
		if (!runTest(javaExpression, !repeatTestAtError, expectedSuggestions) && repeatTestAtError) {
			int numLoggedEntries = logger.getNumberOfLoggedEntries();
			prepareLogger(isPrintLogEntriesAtError(), isStopAtError() ? numLoggedEntries : -1);
			runTest(javaExpression, true, expectedSuggestions);
		}
	}

	void testCompletionWithError(String javaExpression, int caretPosition, Class<? extends Exception> expectedExceptionClass) {
		ParserSettings settings = settingsBuilder.build();

		JavaParser parser = new JavaParser();
		try {
			parser.suggestCodeCompletion(javaExpression, caretPosition, settings, testInstance);
			fail("Expression: " + javaExpression + " - Expected an exception");
		} catch (ParseException | IllegalStateException e) {
			assertEquals(expectedExceptionClass, e.getClass());
		}
	}

	private boolean runTest(String javaExpression, boolean executeAssertions, String... expectedSuggestions) {
		ParserSettings settings = settingsBuilder.build();
		ParserLogger logger = settings.getLogger();

		JavaParser parser = new JavaParser();
		int caretPosition = javaExpression.length();
		List<String> suggestions = null;
		try {
			suggestions = extractSuggestions(parser.suggestCodeCompletion(javaExpression, caretPosition, settings, testInstance));
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

	private static List<String> extractSuggestions(Map<CompletionSuggestion, MatchRating> ratedSuggestions) {
		List<CompletionSuggestion> sortedSuggestions = new ArrayList<>(ratedSuggestions.keySet());
		sortedSuggestions.sort(Comparator.comparingInt(CompletionTest::getCompletionSuggestionPriorityByClass));
		sortedSuggestions.sort(Comparator.comparing(ratedSuggestions::get));
		return sortedSuggestions.stream()
				.map(completion -> completion.getTextToInsert())
				.collect(Collectors.toList());
	}

	/**
	 * Prefer variables ({@link CompletionSuggestionVariable}) over fields ({@link CompletionSuggestionField}) over other suggestions.
	 */
	private static int getCompletionSuggestionPriorityByClass(CompletionSuggestion suggestion) {
		Class<? extends CompletionSuggestion> suggestionClass = suggestion.getClass();
		return	suggestionClass == CompletionSuggestionVariable.class	? 0 :
			suggestionClass == CompletionSuggestionField.class		? 1
				: 2;
	}
}

package dd.kms.zenodot.completionTests.framework;

import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.common.AbstractTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public abstract class CompletionTest extends AbstractTest<CompletionTest>
{
	public static List<CodeCompletion> getSortedCompletions(List<CodeCompletion> completions) {
		List<CodeCompletion> sortedCompletions = new ArrayList<>(completions);
		sortedCompletions.sort(Comparator.comparing(CodeCompletion::getType));
		sortedCompletions.sort(Comparator.comparing(CodeCompletion::getRating));
		return sortedCompletions;
	}

	private static List<String> extractCompletions(List<CodeCompletion> completions) {
		return getSortedCompletions(completions).stream()
			.map(completion -> completion.getTextToInsert())
			.collect(Collectors.toList());
	}

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

	void testCompletion(String expression, String[] expectedCompletions) {
		ParserLogger logger = prepareLogger(false, -1);

		boolean repeatTestAtError = isStopAtError() || isPrintLogEntriesAtError();
		if (!runTest(expression, !repeatTestAtError, expectedCompletions) && repeatTestAtError) {
			int numLoggedEntries = logger.getNumberOfLoggedEntries();
			prepareLogger(isPrintLogEntriesAtError(), isStopAtError() ? numLoggedEntries : -1);
			runTest(expression, true, expectedCompletions);
		}
	}

	void testCompletionWithError(String expression, int caretPosition, Class<? extends Exception> expectedExceptionClass) {
		ParserSettings settings = settingsBuilder.build();

		try {
			ObjectInfo thisInfo = InfoProvider.createObjectInfo(testInstance);
			Parsers.createExpressionParser(expression, settings).getCompletions(thisInfo, caretPosition);
			fail("Expression: " + expression + " - Expected an exception");
		} catch (ParseException | IllegalStateException e) {
			if (e.getClass() != expectedExceptionClass) {
				e.printStackTrace();
				Assert.assertEquals(expectedExceptionClass, e.getClass());
			}
		}
	}

	private boolean runTest(String expression, boolean executeAssertions, String... expectedCompletions) {
		ParserSettings settings = settingsBuilder.build();

		int caretPosition = expression.length();
		List<String> completions;
		try {
			ObjectInfo thisInfo = InfoProvider.createObjectInfo(testInstance);
			completions = extractCompletions(Parsers.createExpressionParser(expression, settings).getCompletions(thisInfo, caretPosition));
		} catch (ParseException e) {
			if (executeAssertions) {
				e.printStackTrace();
				fail("Exception during code completion: " + e.getMessage());
			}
			return false;
		}
		if (executeAssertions) {
			assertTrue(MessageFormat.format("expected completions: {1}, actual completions: {2}",
					expression,
					expectedCompletions,
					completions),
					completions.size() >= expectedCompletions.length);
		}
		if (completions.size() < expectedCompletions.length) {
			return false;
		}

		for (int i = 0; i < expectedCompletions.length; i++) {
			if (executeAssertions) {
				assertEquals(expectedCompletions[i], completions.get(i));
			}
			if (!Objects.equals(expectedCompletions[i], completions.get(i))) {
				return false;
			}
		}
		return true;
	}
}

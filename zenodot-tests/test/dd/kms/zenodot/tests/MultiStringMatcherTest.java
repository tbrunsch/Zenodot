package dd.kms.zenodot.tests;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import dd.kms.zenodot.api.common.RegexUtils;
import dd.kms.zenodot.api.common.multistringmatching.MultiStringMatcher;
import dd.kms.zenodot.api.debug.LogLevel;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tests the {@link MultiStringMatcher}. For the test it suffices to use values
 * identical to the keys.
 */
public class MultiStringMatcherTest
{
	private static final Map<String, Object>	SEARCH_TARGETS_BY_NAME	= ImmutableMap.<String, Object>builder()
		.put("ArrayList", 						ArrayList.class)
		.put("ArrayIndexOutOfBoundsException",	ArrayIndexOutOfBoundsException.class)
		.put("ArrayBlockingQueue",				ArrayBlockingQueue.class)
		.put("AbstractLogger",					"AbstractLogger")
		.put("ArtificialIntelligence",			"AI")
		.put("LinkedList",						LinkedList.class)
		.put("LogLevel",						LogLevel.class)
		.put("Livelock",						"Livelock")
		.put("Logarithm",						"log")
		.build();

	@Test
	public void manualTest() {
		MultiStringMatcher<Object> matcher = new MultiStringMatcher<>();
		for (Map.Entry<String, Object> entry : SEARCH_TARGETS_BY_NAME.entrySet()) {
			matcher.put(entry.getKey(), entry.getValue());
		}

		/*
		 * Successful searches for words starting with "A" without shortcuts
		 */
		testManually(matcher, "A", 								ArrayList.class, ArrayIndexOutOfBoundsException.class, ArrayBlockingQueue.class, "AbstractLogger", "AI");
		testManually(matcher, "Ar", 							ArrayList.class, ArrayIndexOutOfBoundsException.class, ArrayBlockingQueue.class, "AI");
		testManually(matcher, "Arr", 							ArrayList.class, ArrayIndexOutOfBoundsException.class, ArrayBlockingQueue.class);
		testManually(matcher, "Array", 							ArrayList.class, ArrayIndexOutOfBoundsException.class, ArrayBlockingQueue.class);
		testManually(matcher, "ArrayL", 						ArrayList.class);
		testManually(matcher, "ArrayI", 						ArrayIndexOutOfBoundsException.class);
		testManually(matcher, "ArrayB", 						ArrayBlockingQueue.class);
		testManually(matcher, "ArrayList", 						ArrayList.class);
		testManually(matcher, "ArrayIndexOutOfBoundsException", ArrayIndexOutOfBoundsException.class);
		testManually(matcher, "ArrayBlockingQueue", 			ArrayBlockingQueue.class);
		testManually(matcher, "Ab", 							"AbstractLogger");
		testManually(matcher, "AbstractLogger", 				"AbstractLogger");
		testManually(matcher, "Art", 							"AI");
		testManually(matcher, "ArtificialIntelligence", 		"AI");

		/*
		 * Successful searches for words starting with "A" with shortcuts
		 */
		testManually(matcher, "AL", 							ArrayList.class, "AbstractLogger");
		testManually(matcher, "ArL", 							ArrayList.class);
		testManually(matcher, "ALi", 							ArrayList.class);
		testManually(matcher, "ArLi", 							ArrayList.class);
		testManually(matcher, "AbL", 							"AbstractLogger");
		testManually(matcher, "ALo", 							"AbstractLogger");
		testManually(matcher, "AbLo", 							"AbstractLogger");
		testManually(matcher, "AI", 							ArrayIndexOutOfBoundsException.class, "AI");
		testManually(matcher, "ArIn", 							ArrayIndexOutOfBoundsException.class, "AI");
		testManually(matcher, "ArrIn", 							ArrayIndexOutOfBoundsException.class);
		testManually(matcher, "ArInd", 							ArrayIndexOutOfBoundsException.class);
		testManually(matcher, "ArrInd",							ArrayIndexOutOfBoundsException.class);
		testManually(matcher, "ArtIn", 							"AI");
		testManually(matcher, "ArInt", 							"AI");
		testManually(matcher, "ArtInt",							"AI");

		/*
		 * Successful searches for words starting with "L" without shortcuts
		 */
		testManually(matcher, "L",								LinkedList.class, LogLevel.class, "Livelock", "log");
		testManually(matcher, "Li",								LinkedList.class, "Livelock");
		testManually(matcher, "Lin",							LinkedList.class);
		testManually(matcher, "LinkedList",						LinkedList.class);
		testManually(matcher, "Liv",							"Livelock");
		testManually(matcher, "Livelock",						"Livelock");
		testManually(matcher, "Lo",								LogLevel.class, "log");
		testManually(matcher, "Log",							LogLevel.class, "log");
		testManually(matcher, "LogL",							LogLevel.class);
		testManually(matcher, "LogLevel",						LogLevel.class);
		testManually(matcher, "Loga",							"log");
		testManually(matcher, "Logarithm",						"log");

		/*
		 * Successful searches for words starting with "L" with shortcuts
		 */
		testManually(matcher, "LL",								LinkedList.class, LogLevel.class);
		testManually(matcher, "LiL",							LinkedList.class);
		testManually(matcher, "LLi",							LinkedList.class);
		testManually(matcher, "LiLi",							LinkedList.class);
		testManually(matcher, "LinkLis",						LinkedList.class);
		testManually(matcher, "LoL",							LogLevel.class);
		testManually(matcher, "LLe",							LogLevel.class);
		testManually(matcher, "LoLe",							LogLevel.class);
		testManually(matcher, "LoLeve",							LogLevel.class);

		/*
		 * Wildcard searches
		 */
		testManually(matcher, "*", 								ArrayList.class, ArrayIndexOutOfBoundsException.class, ArrayBlockingQueue.class, "AbstractLogger", "AI", LinkedList.class, LogLevel.class, "Livelock", "log");
		testManually(matcher, "A*e", 							ArrayIndexOutOfBoundsException.class, ArrayBlockingQueue.class, "AbstractLogger", "AI");
		testManually(matcher, "A*o", 							ArrayIndexOutOfBoundsException.class, ArrayBlockingQueue.class, "AbstractLogger");
		testManually(matcher, "L*i", 							LinkedList.class, "Livelock", "log");
		testManually(matcher, "L*o",							LogLevel.class, "Livelock", "log");
		testManually(matcher, "L*e", 							LinkedList.class, LogLevel.class, "Livelock");
		testManually(matcher, "L*t", 							LinkedList.class, "log");
		testManually(matcher, "*a", 							ArrayList.class, ArrayIndexOutOfBoundsException.class, ArrayBlockingQueue.class, "AbstractLogger", "AI", "log");
		testManually(matcher, "*i*t", 							ArrayList.class, "AI", LinkedList.class, "log");
		testManually(matcher, "*o",								ArrayIndexOutOfBoundsException.class, ArrayBlockingQueue.class, "AbstractLogger", LogLevel.class, "Livelock", "log");
		testManually(matcher, "*o*e",							ArrayIndexOutOfBoundsException.class, ArrayBlockingQueue.class, "AbstractLogger", LogLevel.class);
		testManually(matcher, "*o*k",							ArrayBlockingQueue.class, "Livelock");

		/*
		 * Unsuccessful searches
		 */
		testManually(matcher, "B");
		testManually(matcher, "I");
		testManually(matcher, "AsL");
		testManually(matcher, "ALu");
		testManually(matcher, "ALT");
		testManually(matcher, "AIS");
		testManually(matcher, "AIm");
		testManually(matcher, "ABQr");
		testManually(matcher, "ABQM");
		testManually(matcher, "ABQM");
		testManually(matcher, "LoLi");
		testManually(matcher, "LP");
		testManually(matcher, "ArrayListX");
		testManually(matcher, "ArrayIndexOutOfBoundsExceptionX");
		testManually(matcher, "ArrayBlockingQueueX");
		testManually(matcher, "AbstractLoggerX");
		testManually(matcher, "ArtificialIntelligenceX");
	}

	private void testManually(MultiStringMatcher<Object> matcher, String keyPattern, Object... expectations) {
		HashSet<Object> expectedResults = Sets.newHashSet(expectations);
		Collection<Object> actualResults = matcher.search(keyPattern);
		checkResult(keyPattern, expectedResults, actualResults);
	}

	@Test
	public void automaticTest() {
		MultiStringMatcher<String> matcher = new MultiStringMatcher<>();

		char[] keyCharacters = { 'a', 'b', 'C', 'D' };
		List<String> keys = generateWords(keyCharacters, 8);
		for (String key : keys) {
			matcher.put(key, key);
		}

		char[] keyPatternCharacters = { 'a', 'c', 'B', 'D'};
		List<String> keyPatterns = generateWords(keyPatternCharacters, 5);
		for (String keyPattern : keyPatterns) {
			Set<String> actualResults = matcher.search(keyPattern);
			Set<String> expectedResults = filterWords(keys, keyPattern);
			checkResult(keyPattern, expectedResults, actualResults);

			if (actualResults.size() > 1) {
				int reducedResultNumber = actualResults.size() / 2;
				Set<String> trimmedResults = matcher.search(keyPattern, reducedResultNumber);
				Assert.assertEquals("Limiting the number of results failed", reducedResultNumber, trimmedResults.size());
			}
		}
	}

	private static List<String> generateWords(char[] characters, int maxLength) {
		List<String> allWords = new ArrayList<>();
		List<String> words = Arrays.asList("");
		for (int length = 1; length <= maxLength; length++) {
			words = appendCharacter(words, characters);
			allWords.addAll(words);
		}
		return allWords;
	}

	private static List<String> appendCharacter(List<String> words, char[] characters) {
		List<String> longerWords = new ArrayList<>(words.size()*characters.length);
		for (String word : words) {
			for (char c : characters) {
				String longerWord = word + c;
				longerWords.add(longerWord);
			}
		}
		return longerWords;
	}

	private static Set<String> filterWords(List<String> words, String wildcardPattern) {
		Pattern pattern = RegexUtils.createRegexForWildcardString(wildcardPattern);
		Set<String> filteredWords = new HashSet<>();
		for (String word : words) {
			Matcher matcher = pattern.matcher(word);
			if (matcher.matches()) {
				filteredWords.add(word);
			}
		}
		return filteredWords;
	}

	@Test
	public void copyTest() {
		MultiStringMatcher<Object> origMatcher = new MultiStringMatcher<>();
		for (Map.Entry<String, Object> entry : SEARCH_TARGETS_BY_NAME.entrySet()) {
			origMatcher.put(entry.getKey(), entry.getValue());
		}

		MultiStringMatcher<Object> clonedMatcher = new MultiStringMatcher<>(origMatcher);

		testSameResults(origMatcher, clonedMatcher, "");
	}

	private static void testSameResults(MultiStringMatcher<Object> expectedMatcher, MultiStringMatcher<Object> actualMatcher, String keyPattern) {
		Set<Object> expectedResults = expectedMatcher.search(keyPattern);
		Set<Object> actualResults = actualMatcher.search(keyPattern);
		checkResult(keyPattern, expectedResults, actualResults);

		if (expectedResults.isEmpty()) {
			return;
		}
		for (char c = 'A'; c <= 'Z'; c++) {
			testSameResults(expectedMatcher, actualMatcher, keyPattern + c);
		}
		for (char c = 'a'; c <= 'z'; c++) {
			testSameResults(expectedMatcher, actualMatcher, keyPattern + c);
		}
	}

	private static <T> void checkResult(String keyPattern, Set<T> expectedResults, Collection<T> actualResults) {
		if (Objects.equals(expectedResults, actualResults)) {
			return;
		}
		for (T expectedResult : expectedResults) {
			if (!actualResults.contains(expectedResult)) {
				Assert.fail("Unexpected result for key pattern \"" + keyPattern + "\": Pattern expected to match \"" + expectedResult + "\"");
			}
		}
		for (T actualResult : actualResults) {
			if (!expectedResults.contains(actualResult)) {
				Assert.fail("Unexpected result for key pattern \"" + keyPattern + "\": Unexpected match \"" + actualResult + "\"");
			}
		}
		Assert.fail("Test setup failed for key pattern \"" + keyPattern + "\"");
	}
}

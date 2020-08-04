package dd.kms.zenodot;

import com.google.common.collect.Sets;
import dd.kms.zenodot.api.common.RegexUtils;
import dd.kms.zenodot.api.common.multistringmatching.MultiStringMatcher;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tests the {@link MultiStringMatcher}. For the test it suffices to use values
 * identical to the keys.
 */
public class MultiStringMatcherTest
{
	@Test
	public void manualTest() {
		MultiStringMatcher<String> matcher = new MultiStringMatcher<>();
		String[] keys = {
				"ArrayList",
				"ArrayIndexOutOfBoundsException",
				"ArrayBlockingQueue",
				"AbstractLogger",
				"ArtificialIntelligence",
				"LinkedList",
				"LogLevel",
				"Livelock",
				"Logarithm"
		};
		for (String key : keys) {
			matcher.put(key, key);
		}

		/*
		 * Successful searches for words starting with "A" without shortcuts
		 */
		testManually(matcher, "A", 								"ArrayList", "ArrayIndexOutOfBoundsException", "ArrayBlockingQueue", "AbstractLogger", "ArtificialIntelligence");
		testManually(matcher, "Ar", 							"ArrayList", "ArrayIndexOutOfBoundsException", "ArrayBlockingQueue", "ArtificialIntelligence");
		testManually(matcher, "Arr", 							"ArrayList", "ArrayIndexOutOfBoundsException", "ArrayBlockingQueue");
		testManually(matcher, "Array", 							"ArrayList", "ArrayIndexOutOfBoundsException", "ArrayBlockingQueue");
		testManually(matcher, "ArrayL", 						"ArrayList");
		testManually(matcher, "ArrayI", 						"ArrayIndexOutOfBoundsException");
		testManually(matcher, "ArrayB", 						"ArrayBlockingQueue");
		testManually(matcher, "ArrayList", 						"ArrayList");
		testManually(matcher, "ArrayIndexOutOfBoundsException", "ArrayIndexOutOfBoundsException");
		testManually(matcher, "ArrayBlockingQueue", 			"ArrayBlockingQueue");
		testManually(matcher, "Ab", 							"AbstractLogger");
		testManually(matcher, "AbstractLogger", 				"AbstractLogger");
		testManually(matcher, "Art", 							"ArtificialIntelligence");
		testManually(matcher, "ArtificialIntelligence", 		"ArtificialIntelligence");

		/*
		 * Successful searches for words starting with "A" with shortcuts
		 */
		testManually(matcher, "AL", 							"ArrayList", "AbstractLogger");
		testManually(matcher, "ArL", 							"ArrayList");
		testManually(matcher, "ALi", 							"ArrayList");
		testManually(matcher, "ArLi", 							"ArrayList");
		testManually(matcher, "AbL", 							"AbstractLogger");
		testManually(matcher, "ALo", 							"AbstractLogger");
		testManually(matcher, "AbLo", 							"AbstractLogger");
		testManually(matcher, "AI", 							"ArrayIndexOutOfBoundsException", "ArtificialIntelligence");
		testManually(matcher, "ArIn", 							"ArrayIndexOutOfBoundsException", "ArtificialIntelligence");
		testManually(matcher, "ArrIn", 							"ArrayIndexOutOfBoundsException");
		testManually(matcher, "ArInd", 							"ArrayIndexOutOfBoundsException");
		testManually(matcher, "ArrInd",							"ArrayIndexOutOfBoundsException");
		testManually(matcher, "ArtIn", 							"ArtificialIntelligence");
		testManually(matcher, "ArInt", 							"ArtificialIntelligence");
		testManually(matcher, "ArtInt",							"ArtificialIntelligence");

		/*
		 * Successful searches for words starting with "L" without shortcuts
		 */
		testManually(matcher, "L",								"LinkedList", "LogLevel", "Livelock", "Logarithm");
		testManually(matcher, "Li",								"LinkedList", "Livelock");
		testManually(matcher, "Lin",							"LinkedList");
		testManually(matcher, "LinkedList",						"LinkedList");
		testManually(matcher, "Liv",							"Livelock");
		testManually(matcher, "Livelock",						"Livelock");
		testManually(matcher, "Lo",								"LogLevel", "Logarithm");
		testManually(matcher, "Log",							"LogLevel", "Logarithm");
		testManually(matcher, "LogL",							"LogLevel");
		testManually(matcher, "LogLevel",						"LogLevel");
		testManually(matcher, "Loga",							"Logarithm");
		testManually(matcher, "Logarithm",						"Logarithm");

		/*
		 * Successful searches for words starting with "L" with shortcuts
		 */
		testManually(matcher, "LL",								"LinkedList", "LogLevel");
		testManually(matcher, "LiL",							"LinkedList");
		testManually(matcher, "LLi",							"LinkedList");
		testManually(matcher, "LiLi",							"LinkedList");
		testManually(matcher, "LinkLis",						"LinkedList");
		testManually(matcher, "LoL",							"LogLevel");
		testManually(matcher, "LLe",							"LogLevel");
		testManually(matcher, "LoLe",							"LogLevel");
		testManually(matcher, "LoLeve",							"LogLevel");

		/*
		 * Wildcard searches
		 */
		testManually(matcher, "*", 								"ArrayList", "ArrayIndexOutOfBoundsException", "ArrayBlockingQueue", "AbstractLogger", "ArtificialIntelligence", "LinkedList", "LogLevel", "Livelock", "Logarithm");
		testManually(matcher, "A*e", 							"ArrayIndexOutOfBoundsException", "ArrayBlockingQueue", "AbstractLogger", "ArtificialIntelligence");
		testManually(matcher, "A*o", 							"ArrayIndexOutOfBoundsException", "ArrayBlockingQueue", "AbstractLogger");
		testManually(matcher, "L*i", 							"LinkedList", "Livelock", "Logarithm");
		testManually(matcher, "L*o",							"LogLevel", "Livelock", "Logarithm");
		testManually(matcher, "L*e", 							"LinkedList", "LogLevel", "Livelock");
		testManually(matcher, "L*t", 							"LinkedList", "Logarithm");
		testManually(matcher, "*a", 							"ArrayList", "ArrayIndexOutOfBoundsException", "ArrayBlockingQueue", "AbstractLogger", "ArtificialIntelligence", "Logarithm");
		testManually(matcher, "*i*t", 							"ArrayList", "ArtificialIntelligence", "LinkedList", "Logarithm");
		testManually(matcher, "*o",								"ArrayIndexOutOfBoundsException", "ArrayBlockingQueue", "AbstractLogger", "LogLevel", "Livelock", "Logarithm");
		testManually(matcher, "*o*e",							"ArrayIndexOutOfBoundsException", "ArrayBlockingQueue", "AbstractLogger", "LogLevel");
		testManually(matcher, "*o*k",							"ArrayBlockingQueue", "Livelock");

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

	private void testManually(MultiStringMatcher<String> matcher, String keyPattern, String... expectations) {
		HashSet<String> expectedResults = Sets.newHashSet(expectations);
		Collection<String> actualResults = matcher.search(keyPattern);
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

	private static void checkResult(String keyPattern, Set<String> expectedResults, Collection<String> actualResults) {
		if (Objects.equals(expectedResults, actualResults)) {
			return;
		}
		for (String expectedResult : expectedResults) {
			if (!actualResults.contains(expectedResult)) {
				Assert.fail("Unexpected result for key pattern \"" + keyPattern + "\": Pattern expected to match \"" + expectedResult + "\"");
			}
		}
		for (String actualResult : actualResults) {
			if (!expectedResults.contains(actualResult)) {
				Assert.fail("Unexpected result for key pattern \"" + keyPattern + "\": Unexpected match \"" + actualResult + "\"");
			}
		}
		Assert.fail("Test setup failed for key pattern \"" + keyPattern + "\"");
	}
}

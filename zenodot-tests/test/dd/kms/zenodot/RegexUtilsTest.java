package dd.kms.zenodot;

import dd.kms.zenodot.common.RegexUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

public class RegexUtilsTest
{
	@Test
	public void testPrefix() {
		expectMatch("wi", "wildcard", "wizard", "winter");
		expectMismatch("wi", "w", "welcome", "abc", "QWERT");
	}

	@Test
	public void testAsterisk() {
		expectMatch("", "", "abc", "QWERT");
		expectMatch("*", "", "abc", "QWERT");

		expectMatch("wi*ard", "wildcard", "wizard", "win easily lose hardly");
		expectMismatch("wi*ard", "we work hard", "winner", "abc", "");
	}

	@Test
	public void testCapitalLetters() {
		expectMatch("ArrL", "ArrayList", "Arrow Left", "ArrL");
		expectMismatch("ArrL", "myArrayList", "ArrestTheLord", "ArL");

		expectMatch("BC", "BC", "BeCe", "BCd", "BCD", "BδC");
		expectMismatch("BC", "aBC", "ABC", "BIC", "BΔC");
	}

	@Test
	public void testAsteriskAndCapitalLetters() {
		expectMatch("*ArrL", "myArrayList");
		expectMatch("Arr*L", "ArrestTheLord");
		expectMismatch("*Arr*L", "ArL");

		expectMatch("*BC", "aBC", "ABC");
		expectMatch("B*C", "BIC", "BΔC");
	}

	@Test
	public void testSpecialCharacters() {
		expectMatch("e^{iπ} = -1?", "e^{iπ} = -1?");
		expectMatch("7-2+6 > 10", "7-2+6 > 10");
	}

	private static void expectMatch(String wildcardString, String... strings) {
		Pattern regex = RegexUtils.createRegexForWildcardString(wildcardString);
		for (String s : strings) {
			Assert.assertTrue("Wildcard string '" + wildcardString + "' does not match string '" + s + "'", regex.matcher(s).matches());
		}
	}

	private static void expectMismatch(String wildcardString, String... strings) {
		Pattern regex = RegexUtils.createRegexForWildcardString(wildcardString);
		for (String s : strings) {
			Assert.assertFalse("Wildcard string '" + wildcardString + "' matches string '" + s + "'", regex.matcher(s).matches());
		}
	}
}

package dd.kms.zenodot.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class RegexUtils
{
	private static final Set<Character> SPECIAL_REGEX_CHARACTERS	= Sets.newHashSet(Lists.charactersOf("\\.[]{}()<>+-=?^$|*"));

	public static String escapeIfSpecial(char c) {
		return SPECIAL_REGEX_CHARACTERS.contains(c) ? "\\" + c : String.valueOf(c);
	}

	public static Pattern createRegexForWildcardString(String wildcardString) {
		StringBuilder builder = new StringBuilder();
		int numChars = wildcardString.length();
		for (int i = 0; i < numChars; i++) {
			char c = wildcardString.charAt(i);

			if (c == '*') {
				// wild card
				builder.append(".*");
			} else if (Character.isUpperCase(c)) {
				/*
				 * Insert wild cards before upper-case characters (except for the first character) as known from common IDEs
				 *
				 * Example: "ArrLi" will should also match "ArrayList", but not "xArrLi", so the corresponding regex should be "Arr[a-z0-9]*Li"
				 */
				if (i > 0) {
					builder.append("[a-z0-9\\s]*");
				}
				builder.append(c);
			} else {
				builder.append(escapeIfSpecial(c));
			}
		}
		// wild card at the end to allow arbitrary suffixes
		builder.append(".*");
		return Pattern.compile(builder.toString());
	}
}

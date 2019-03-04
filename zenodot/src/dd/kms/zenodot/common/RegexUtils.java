package dd.kms.zenodot.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Set;
import java.util.regex.Pattern;

public class RegexUtils
{
	private static final Set<Character> SPECIAL_REGEX_CHARACTERS	= Sets.newHashSet(Lists.charactersOf("\\.[]{}()<>+-=?^$|*"));

	public static String escapeIfSpecial(char c) {
		return SPECIAL_REGEX_CHARACTERS.contains(c) ? "\\" + c : String.valueOf(c);
	}

	/**
	 * Returns a regex pattern for a wildcard string.<br/>
	 * <br/>
	 * A wildcard string matches a string if it is the prefix of that string. There are two exceptions to this rule:
	 * <ul>
	 *     <li>
	 *			The asterisk symbol {@code *} in a wildcard string matches any (possibly empty) sequence of characters.
	 *			For example, the wildcard string {@code wi*ard} matches the strings {@code wildcard} and {@code wizard}.
	 *     </li>
	 *     <li>
	 *			As known from common IDEs, capital letters in wildcard strings play a special role. For example,
	 *			the wildcard string {@code ArrLi} matches the string {@code ArrayList}.
	 *     </li>
	 * </ul>
	 */
	public static Pattern createRegexForWildcardString(String wildcardString) {
		StringBuilder builder = new StringBuilder();
		int numChars = wildcardString.length();
		for (int i = 0; i < numChars; i++) {
			char c = wildcardString.charAt(i);

			if (c == '*') {
				// wild card
				builder.append(".*");
			} else if (Character.isUpperCase(c)) {
				if (i > 0) {
					// Do not insert placeholder at first position to prevent, e.g., wildcard string "ArrLi" from matching "myArrayList"
					builder.append("[^\\p{javaUpperCase}]*");
				}
				builder.append(c);
			} else {
				builder.append(escapeIfSpecial(c));
			}
		}
		// wildcard at the end to allow arbitrary suffixes
		builder.append(".*");
		return Pattern.compile(builder.toString());
	}
}

package dd.kms.zenodotx.rule.simple;

import dd.kms.zenodot.api.common.RegexUtils;

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SyntaxRules
{
	public static SyntaxRule forCharacter(char c) {
		String escapedCharacter = RegexUtils.escapeIfSpecial(c);
		Pattern pattern = Pattern.compile(escapedCharacter);
		return new SyntaxRule() {
			@Override
			public Pattern getRegex() {
				return pattern;
			}

			@Override
			public String getSyntaxDescription() {
				return "'" + c + "'";
			}
		};
	}

	public static Pattern getPatternForString(String s) {
		String regex = RegexUtils.escapeIfSpecial(s);
		return Pattern.compile(regex);
	}

	public static Pattern getPatternForStringAlternatives(Collection<String> strings) {
		String regex = strings.stream()
			.map(s -> "(" + RegexUtils.escapeIfSpecial(s) + ")")
			.collect(Collectors.joining("|"));
		return Pattern.compile(regex);
	}

	public static SyntaxRule space() {
		Pattern pattern = Pattern.compile("\\s+");
		return new SyntaxRule() {
			@Override
			public Pattern getRegex() {
				return pattern;
			}

			@Override
			public String getSyntaxDescription() {
				return "Whitespace";
			}
		};
	}
}

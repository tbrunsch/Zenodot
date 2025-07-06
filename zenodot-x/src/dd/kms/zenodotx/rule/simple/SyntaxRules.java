package dd.kms.zenodotx.rule.simple;

import dd.kms.zenodot.api.common.RegexUtils;

import java.util.regex.Pattern;

public class SyntaxRules
{
	private static final Pattern	EMPTY_PATTERN	= Pattern.compile("");
	public static final SyntaxRule	EMPTY			= new SyntaxRule() {
		@Override
		public Pattern getRegex() {
			return EMPTY_PATTERN;
		}

		@Override
		public String getSyntaxDescription() {
			return "Empty string";
		}
	};

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

	public static SyntaxRule forString(String s) {
		StringBuilder builder = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			String escapedCharacter = RegexUtils.escapeIfSpecial(c);
			builder.append(escapedCharacter);
		}
		Pattern pattern = Pattern.compile(builder.toString());
		return new SyntaxRule() {
			@Override
			public Pattern getRegex() {
				return pattern;
			}

			@Override
			public String getSyntaxDescription() {
				return "\"" + s + "\"";
			}
		};
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

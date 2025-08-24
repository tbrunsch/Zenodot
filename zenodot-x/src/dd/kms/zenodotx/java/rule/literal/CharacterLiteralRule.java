package dd.kms.zenodotx.java.rule.literal;

import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.java.rule.AbstractSemanticJavaRule;
import dd.kms.zenodotx.java.rule.ConstantParseResult;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CharacterLiteralRule extends AbstractRule<Void, InstanceParseResult, JavaSettings> implements SimpleRule<Void, InstanceParseResult, JavaSettings>
{
	private static final String		CHARACTER_LITERAL_REGEX	=	"'("
																		+ LiteralUtils.INTERPRETATION_OF_ESCAPED_CHARACTERS.keySet().stream()
																		.map(c -> "\\" + c)
																		.collect(Collectors.joining("|"))
																		+ "|" + "[^\\\\']"
																	+ ")'";
	private static final Pattern	CHARACTER_LITERAL_PATTERN	= Pattern.compile(CHARACTER_LITERAL_REGEX);

	@Override
	public SyntaxRule getSyntaxRule() {
		return new SyntaxRule() {
			@Override
			public Pattern getRegex() {
				return CHARACTER_LITERAL_PATTERN;
			}

			@Override
			public String getSyntaxDescription() {
				return "character literal";
			}
		};
	}

	@Override
	public SemanticRule<Void, InstanceParseResult, JavaSettings> getSemanticRule() {
		return new AbstractSemanticJavaRule<Void, InstanceParseResult>() {

			@Override
			protected void doSuggestCodeCompletions(Void input, String parsedString, JavaSettings settings) {
				/* nothing to do */
			}

			@Override
			protected void doSuggestMethodParameters(Void input, JavaSettings settings) {
				/* nothing to do */
			}

			@Override
			public InstanceParseResult evaluate(Void input, String parsedString, JavaSettings settings) throws SemanticException, EvaluationException {
				int numChars = parsedString.length();
				if (parsedString.startsWith("'") && numChars >= 3 && parsedString.endsWith("'")){
					String escapedCharacterLiteral = parsedString.substring(1, numChars - 1);
					String unescapedCharacterLiteral;
					try {
						unescapedCharacterLiteral = LiteralUtils.unescapeCharacters(escapedCharacterLiteral);
						if (unescapedCharacterLiteral.length() == 1) {
							ObjectInfo characterInfo = InfoProvider.createObjectInfo(unescapedCharacterLiteral.charAt(0), char.class);
							return new ConstantParseResult(characterInfo);
						}
					} catch (IllegalArgumentException e) {
						/* fallthrough */
					}
				}
				throw new IllegalStateException("Unexpected character literal: " + parsedString);
			}
		};
	}
}

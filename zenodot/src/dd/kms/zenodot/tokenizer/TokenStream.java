package dd.kms.zenodot.tokenizer;

import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * View on a given Java expression in form of a stream of {@link Token}s. The {@code TokenStream}
 * is not responsible for determining how the expression has to be split into tokens. It is the parsers'
 * responsibility to query the next token with the correct type. If a parser expects a token type
 * that is not available, then the parser is not the right one for parsing the current subexpression
 * and the parsing framework will try another parser.
 */
public class TokenStream implements Cloneable
{
	private static final Pattern		OPTIONAL_SPACE					= Pattern.compile("^(\\s*).*");
	private static final Pattern		CHARACTER_PATTERN				= Pattern.compile("^(\\s*([^\\s])\\s*).*");
	private static final Pattern		CHARACTERS_PATTERN				= Pattern.compile("^(\\s*([^\\s]+)\\s*).*");
	private static final Pattern		IDENTIFIER_PATTERN  			= Pattern.compile("^(\\s*([A-Za-z][_A-Za-z0-9]*)\\s*).*");
	private static final Pattern		STRING_LITERAL_PATTERN			= Pattern.compile("^(\\s*\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"\\s*).*");
	private static final Pattern		CHARACTER_LITERAL_PATTERN		= Pattern.compile("^(\\s*'(\\\\.|[^\\\\])'\\s*).*");
	private static final Pattern		KEYWORD_PATTERN					= IDENTIFIER_PATTERN;
	private static final Pattern		INTEGER_LITERAL_PATTERN			= Pattern.compile("^(\\s*(0|[1-9][0-9]*)\\s*)($|[^0-9dDeEfFL].*)");
	private static final Pattern		LONG_LITERAL_PATTERN			= Pattern.compile("^(\\s*(0|[1-9][0-9]*)[lL]\\s*).*");
	private static final Pattern		FLOAT_LITERAL_PATTERN 			= Pattern.compile("^(\\s*(([0-9]+([eE][+-]?[0-9]+)?|\\.[0-9]+([eE][+-]?[0-9]+)?|[0-9]+\\.[0-9]*([eE][+-]?[0-9]+)?)[fF])\\s*).*");
	private static final Pattern		DOUBLE_LITERAL_PATTERN 			= Pattern.compile("^(\\s*(([0-9]+(([eE][+-]?[0-9]+)?[dD]|[eE][+-]?[0-9]+[dD]?)|\\.[0-9]+([eE][+-]?[0-9]+)?[dD]?|[0-9]+\\.[0-9]*([eE][+-]?[0-9]+)?[dD]?))\\s*).*");
	private static final Pattern		PACKAGE_NAME_PATTERN			= Pattern.compile("^(\\s*(\\d*[A-Za-z][_A-Za-z0-9]*)\\s*).*");	// although uncommon, there exist packages starting with an upper-case letter
	private static final Pattern		CLASS_NAME_PATTERN				= PACKAGE_NAME_PATTERN;											// although uncommon, there exist class names starting with a lower-case letter

	// Unary prefix operators, sorted from longest to shortest to ensure that, e.g., "++" is tested before "+"
	private static final List<String> 	UNARY_OPERATORS 			= Arrays.stream(UnaryOperator.values()).map(UnaryOperator::getOperator).sorted(Comparator.comparingInt(String::length).reversed()).collect(Collectors.toList());

	// Binary operators, sorted from longest to shortest to ensure that, e.g., "==" is tested before "="
	private static final List<String> 	BINARY_OPERATORS 			= Arrays.stream(BinaryOperator.values()).map(BinaryOperator::getOperator).sorted(Comparator.comparingInt(String::length).reversed()).collect(Collectors.toList());

	private static final Map<Character, Character> INTERPRETATION_OF_ESCAPED_CHARACTERS = ImmutableMap.<Character, Character>builder()
		.put('t', '\t')
		.put('b', '\b')
		.put('n', '\n')
		.put('r', '\r')
		.put('f', '\f')
		.put('\'', '\'')
		.put('\"', '\"')
		.put('\\', '\\')
		.build();

	private static String unescapeCharacters(String s) throws IllegalArgumentException {
		StringBuffer unescapedString = new StringBuffer();
		int pos = 0;
		while (pos < s.length()) {
			char c = s.charAt(pos);
			if (c == '\\') {
				if (pos + 1 == s.length()) {
					throw new IllegalArgumentException("String ends with backslash '\\'");
				}
				char escapedChar = s.charAt(pos + 1);
				Character interpretation = INTERPRETATION_OF_ESCAPED_CHARACTERS.get(escapedChar);
				if (interpretation == null) {
					throw new IllegalArgumentException("Unknown escape sequence: \\" + escapedChar);
				}
				unescapedString.append((char) interpretation);
				pos += 2;
			} else {
				unescapedString.append(c);
				pos++;
			}
		}
		return unescapedString.toString();
	}

	private final String	expression;
	private final int		caretPosition;

	private int				position;

	public TokenStream(String expression, int caretPosition) {
		this(expression, caretPosition, 0);
	}

	private TokenStream(String expression, int caretPosition, int position) {
		this.expression = expression;
		this.caretPosition = caretPosition;
		this.position = position;
	}

	public boolean hasMore() {
		return position < expression.length() && CHARACTER_PATTERN.matcher(expression.substring(position)).matches();
	}

	public int getPosition() {
		return position;
	}

	public boolean isCaretWithinNextWhiteSpaces() {
		int curPosition = position;
		try {
			return position == caretPosition || readOptionalSpace().isContainsCaret();
		} finally {
			position = curPosition;
		}
	}

	public Token readIdentifier() throws JavaTokenParseException {
		return readRegex(IDENTIFIER_PATTERN, 2, "No identifier found");
	}

	public Token readStringLiteral() throws JavaTokenParseException {
		Token escapedStringLiteralToken = readRegex(STRING_LITERAL_PATTERN, 2, "No string literal found");
		return unescapeStringToken(escapedStringLiteralToken);
	}

	public Token readCharacterLiteral() throws JavaTokenParseException {
		Token escapedCharacterLiteralToken = readRegex(CHARACTER_LITERAL_PATTERN, 2, "No character literal found");
		return unescapeStringToken(escapedCharacterLiteralToken);
	}

	public Token readKeyWordUnchecked() {
		return readRegexUnchecked(KEYWORD_PATTERN, 2);
	}

	public Token readIntegerLiteral() throws JavaTokenParseException {
		return readRegex(INTEGER_LITERAL_PATTERN, 2, "No integer literal found");
	}

	public Token readLongLiteral() throws JavaTokenParseException {
		return readRegex(LONG_LITERAL_PATTERN, 2, "No long literal found");
	}

	public Token readFloatLiteral() throws JavaTokenParseException {
		return readRegex(FLOAT_LITERAL_PATTERN, 2, "No float literal found");
	}

	public Token readDoubleLiteral() throws JavaTokenParseException {
		return readRegex(DOUBLE_LITERAL_PATTERN, 2, "No double literal found");
	}

	public Token readPackage() throws JavaTokenParseException {
		return readRegex(PACKAGE_NAME_PATTERN, 2, "No package name found");
	}

	public Token readClass() throws JavaTokenParseException {
		return readRegex(CLASS_NAME_PATTERN, 2, "No class name found");
	}

	private Token unescapeStringToken(Token stringToken) throws JavaTokenParseException {
		String escapedString = stringToken.getValue();
		try {
			String unescapedString = unescapeCharacters(escapedString);
			return new Token(unescapedString, stringToken.isContainsCaret());
		} catch (IllegalArgumentException e) {
			throw new JavaTokenParseException(e.getMessage());
		}
	}

	private Token readRegex(Pattern regex, int groupIndexToExtract, String errorMessage) throws JavaTokenParseException {
		Matcher matcher = regex.matcher(expression.substring(position));
		if (!matcher.matches()) {
			throw new JavaTokenParseException(errorMessage);
		}
		String extractedString = matcher.group(groupIndexToExtract);
		String stringWithSpaces = matcher.group(1);
		int length = stringWithSpaces.length();
		boolean containsCaret = moveForward(length);
		return new Token(extractedString, containsCaret);
	}

	public Token readRegexUnchecked(Pattern regex, int groupIndexToExtract) {
		try {
			return readRegex(regex, groupIndexToExtract, null);
		} catch (JavaTokenParseException e) {
			return null;
		}
	}

	public char peekCharacter() {
		String characters = peekCharacters();
		return characters == null ? 0 : characters.charAt(0);
	}

	public String peekCharacters() {
		Matcher matcher = CHARACTERS_PATTERN.matcher(expression.substring(position));
		return matcher.matches() ? matcher.group(2) : null;
	}

	public Token readCharacterUnchecked() {
		return readRegexUnchecked(CHARACTER_PATTERN, 2);
	}

	public Token readOptionalSpace() { return readRegexUnchecked(OPTIONAL_SPACE, 1); }

	public Token readUnaryOperatorUnchecked() {
		return readOperatorUnchecked(UNARY_OPERATORS);
	}

	public Token readBinaryOperatorUnchecked() {
		return readOperatorUnchecked(BINARY_OPERATORS);
	}

	private Token readOperatorUnchecked(List<String> availableOperators) {
		boolean containsCaret = false;

		containsCaret |= readOptionalSpace().isContainsCaret();

		int expressionLength = expression.length();
		String detectedOperator = null;
		for (String operator : availableOperators) {
			int endIndex = position + operator.length();
			if (endIndex <= expressionLength && operator.equals(expression.substring(position, endIndex))) {
				detectedOperator = operator;
				break;
			}
		}

		if (detectedOperator == null) {
			return null;
		}

		containsCaret |= moveForward(detectedOperator.length());

		containsCaret |= readRegexUnchecked(OPTIONAL_SPACE, 1).isContainsCaret();

		return new Token(detectedOperator, containsCaret);
	}

	/**
	 * Returns true if the caret is encountered when moving from position to nextPosition
	 */
	private boolean moveForward(int numCharacters) {
		int newPosition = position + numCharacters;
		boolean containsCaret = position < caretPosition && caretPosition <= newPosition;
		moveTo(newPosition);
		return containsCaret;
	}

	public void moveTo(int newPosition) {
		position = newPosition;
	}

	@Override
	public TokenStream clone() {
		return new TokenStream(expression, caretPosition, position);
	}

	@Override
	public String toString() {
		return expression.substring(0, position)
				+ "^"
				+ expression.substring(position);
	}

	public static class JavaTokenParseException extends Exception
	{
		JavaTokenParseException(String message) {
			super(message);
		}
	}
}

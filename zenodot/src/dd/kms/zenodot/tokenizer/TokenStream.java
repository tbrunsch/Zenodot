package dd.kms.zenodot.tokenizer;

import com.google.common.collect.ImmutableMap;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
	private static final Pattern		PACKAGE_OR_CLASS_NAME_PATTERN	= Pattern.compile("^(\\s*(\\d*[A-Za-z][_A-Za-z0-9]*)\\s*).*");

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

	private final String	javaExpression;
	private final int		caret;

	private int				position;

	public TokenStream(String javaExpression, int caret) {
		this(javaExpression, caret, 0);
	}

	private TokenStream(String javaExpression, int caret, int position) {
		this.javaExpression = javaExpression;
		this.caret = caret;
		this.position = position;
	}

	public boolean hasMore() {
		return position < javaExpression.length() && CHARACTER_PATTERN.matcher(javaExpression.substring(position)).matches();
	}

	public int getPosition() {
		return position;
	}

	public boolean isCaretAtPosition() {
		return position == caret;
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

	public Token readPackageOrClass() throws JavaTokenParseException {
		return readRegex(PACKAGE_OR_CLASS_NAME_PATTERN, 2, "No package or class name found");
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
		Matcher matcher = regex.matcher(javaExpression.substring(position));
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
		Matcher matcher = CHARACTERS_PATTERN.matcher(javaExpression.substring(position));
		return matcher.matches() ? matcher.group(2) : null;
	}

	public Token readCharacterUnchecked() {
		return readRegexUnchecked(CHARACTER_PATTERN, 2);
	}

	public Token readUnaryOperatorUnchecked() {
		return readOperatorUnchecked(UNARY_OPERATORS);
	}

	public Token readBinaryOperatorUnchecked() {
		return readOperatorUnchecked(BINARY_OPERATORS);
	}

	private Token readOperatorUnchecked(List<String> availableOperators) {
		boolean containsCaret = false;

		containsCaret |= readRegexUnchecked(OPTIONAL_SPACE, 1).isContainsCaret();

		int expressionLength = javaExpression.length();
		String detectedOperator = null;
		for (String operator : availableOperators) {
			int endIndex = position + operator.length();
			if (endIndex <= expressionLength && operator.equals(javaExpression.substring(position, endIndex))) {
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
		boolean containsCaret = position < caret && caret <= newPosition;
		moveTo(newPosition);
		return containsCaret;
	}

	public void moveTo(int newPosition) {
		position = newPosition;
	}

	@Override
	public TokenStream clone() {
		return new TokenStream(javaExpression, caret, position);
	}

	@Override
	public String toString() {
		return javaExpression.substring(0, position)
				+ "^"
				+ javaExpression.substring(position);
	}

	public static class JavaTokenParseException extends Exception
	{
		JavaTokenParseException(String message) {
			super(message);
		}
	}
}
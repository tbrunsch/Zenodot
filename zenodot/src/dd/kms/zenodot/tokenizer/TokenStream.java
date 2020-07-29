package dd.kms.zenodot.tokenizer;

import com.google.common.collect.ImmutableMap;
import dd.kms.zenodot.common.RegexUtils;
import dd.kms.zenodot.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.flowcontrol.InternalErrorException;
import dd.kms.zenodot.flowcontrol.SyntaxException;
import dd.kms.zenodot.result.CodeCompletions;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
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
public class TokenStream
{
	public static final char				EMPTY_CHARACTER					= 0;

	public static final CompletionGenerator	NO_COMPLETIONS					= info -> CodeCompletions.NONE;

	private static final Pattern 			WHITESPACE_PATTERN				= Pattern.compile("^(\\s*).*");
	private static final Pattern 			REMAINING_WHITESPACE_PATTERN	= Pattern.compile("^(\\s*)$");
	private static final Pattern			CHARACTERS_PATTERN				= Pattern.compile("^\\s*([^\\s]*).*");
	private static final Pattern			IDENTIFIER_PATTERN  			= Pattern.compile("^(([_\\$A-Za-z][_\\$A-Za-z0-9]*)\\s*).*");
	private static final Pattern			STRING_LITERAL_PATTERN			= Pattern.compile("^(\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"\\s*).*");
	private static final Pattern			CHARACTER_LITERAL_PATTERN		= Pattern.compile("^('(\\\\.|[^\\\\])'\\s*).*");
	private static final Pattern			KEYWORD_PATTERN					= IDENTIFIER_PATTERN;
	private static final Pattern			INTEGER_LITERAL_PATTERN			= Pattern.compile("^((0|[1-9][0-9]*)\\s*)($|[^0-9dDeEfFL\\.].*)");
	private static final Pattern			LONG_LITERAL_PATTERN			= Pattern.compile("^((0|[1-9][0-9]*)[lL]\\s*).*");
	private static final Pattern			FLOAT_LITERAL_PATTERN 			= Pattern.compile("^((([0-9]+([eE][+-]?[0-9]+)?|\\.[0-9]+([eE][+-]?[0-9]+)?|[0-9]+\\.[0-9]*([eE][+-]?[0-9]+)?)[fF])\\s*).*");
	private static final Pattern			DOUBLE_LITERAL_PATTERN 			= Pattern.compile("^((([0-9]+(([eE][+-]?[0-9]+)?[dD]|[eE][+-]?[0-9]+[dD]?)|\\.[0-9]+([eE][+-]?[0-9]+)?[dD]?|[0-9]+\\.[0-9]*([eE][+-]?[0-9]+)?[dD]?))\\s*).*");
	private static final Pattern			PACKAGE_NAME_PATTERN			= Pattern.compile("^((\\d*[A-Za-z][_A-Za-z0-9]*)\\s*).*");	// although uncommon, there exist packages starting with an upper-case letter
	private static final Pattern			CLASS_NAME_PATTERN				= PACKAGE_NAME_PATTERN;										// although uncommon, there exist class names starting with a lower-case letter

	// Unary prefix operators, sorted from longest to shortest to ensure that, e.g., "++" is tested before "+"
	private static final List<String> 		UNARY_OPERATORS 				= getOperators(UnaryOperator.values(), UnaryOperator::getOperator);

	// Binary operators, sorted from longest to shortest to ensure that, e.g., "==" is tested before "="
	private static final List<String> 		BINARY_OPERATORS 				= getOperators(BinaryOperator.values(), BinaryOperator::getOperator);

	private static final Map<Character, Character>	INTERPRETATION_OF_ESCAPED_CHARACTERS = ImmutableMap.<Character, Character>builder()
		.put('t', '\t')
		.put('b', '\b')
		.put('n', '\n')
		.put('r', '\r')
		.put('f', '\f')
		.put('\'', '\'')
		.put('\"', '\"')
		.put('\\', '\\')
		.build();

	private static final Map<String, Pattern>	CHARACTERS_TO_PATTERN	= new HashMap<>();

	private static Pattern getOrCreateSingleCharacterPattern(char... allowedCharacters) {
		String s = String.valueOf(allowedCharacters);
		Pattern cachedPattern = CHARACTERS_TO_PATTERN.get(s);
		if (cachedPattern != null) {
			return cachedPattern;
		}
		StringBuilder regexBuilder = new StringBuilder("(([");
		boolean allowEmptyCharacter = false;
		for (char c : allowedCharacters) {
			if (c == EMPTY_CHARACTER) {
				allowEmptyCharacter = true;
			} else {
				String escapedCharacter = RegexUtils.escapeIfSpecial(c);
				regexBuilder.append(escapedCharacter);
			}
		}
		regexBuilder.append("]");
		if (allowEmptyCharacter) {
			regexBuilder.append("?");
		}
		regexBuilder.append(")).*");
		Pattern pattern = Pattern.compile(regexBuilder.toString());
		CHARACTERS_TO_PATTERN.put(s, pattern);
		return pattern;
	}

	/**
	 * returns a {@link List} of operators, sorted according to their length (descending)
	 */
	private static <T> List<String> getOperators(T[] operatorValues, Function<T, String> operatorGetter) {
		return Arrays.stream(operatorValues)
			.map(operatorGetter)
			.sorted(Comparator.comparingInt(String::length).reversed())
			.collect(Collectors.toList());
	}

	private String unescapeCharacters(String s) throws SyntaxException {
		StringBuffer unescapedString = new StringBuffer();
		int pos = 0;
		while (pos < s.length()) {
			char c = s.charAt(pos);
			if (c == '\\') {
				if (pos + 1 == s.length()) {
					throw new SyntaxException("The literal ends with a backslash '\\'");
				}
				char escapedChar = s.charAt(pos + 1);
				Character interpretation = INTERPRETATION_OF_ESCAPED_CHARACTERS.get(escapedChar);
				if (interpretation == null) {
					throw new SyntaxException("The literal contains an unknown escape sequence: \\" + escapedChar);
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
		this.expression = expression;
		this.caretPosition = caretPosition < 0 ? Integer.MAX_VALUE : caretPosition;
		this.position = 0;
	}

	public String readIdentifier(CompletionGenerator completionGenerator, String errorMessage) throws SyntaxException, CodeCompletionException {
		return readRegex(IDENTIFIER_PATTERN, 2, completionGenerator, errorMessage, true);
	}

	public String readKeyword(CompletionGenerator completionGenerator, String errorMessage) throws SyntaxException, CodeCompletionException {
		return readRegex(KEYWORD_PATTERN, 2, completionGenerator, errorMessage, true);
	}

	public String readPackage(CompletionGenerator completionGenerator) throws SyntaxException, CodeCompletionException {
		return readRegex(PACKAGE_NAME_PATTERN, 2, completionGenerator, "Expected a package name", true);
	}

	public String readClass(CompletionGenerator completionGenerator) throws SyntaxException, CodeCompletionException {
		return readRegex(CLASS_NAME_PATTERN, 2, completionGenerator, "Expected a class name", true);
	}

	public String readStringLiteral() throws SyntaxException, CodeCompletionException {
		String escapedStringLiteral = readRegex(STRING_LITERAL_PATTERN, 2, NO_COMPLETIONS, "No string literal found", true);
		return unescapeCharacters(escapedStringLiteral);
	}

	public char readCharacterLiteral() throws SyntaxException, CodeCompletionException, InternalErrorException {
		String escapedCharacterLiteral = readRegex(CHARACTER_LITERAL_PATTERN, 2, NO_COMPLETIONS, "No character literal found", true);
		String characterLiteral = unescapeCharacters(escapedCharacterLiteral);
		int length = characterLiteral.length();
		if (length == 0) {
			throw new InternalErrorException(toString() + ": The literal '" + characterLiteral + "' is empty");
		} else if (length > 1) {
			throw new InternalErrorException(toString() + ": The literal '" + characterLiteral + "' consists of " + length + " characters");
		}
		return characterLiteral.charAt(0);
	}

	public int readIntegerLiteral() throws SyntaxException, CodeCompletionException {
		String integerLiteral = readRegex(INTEGER_LITERAL_PATTERN, 2, NO_COMPLETIONS, "Expected an integer literal", true);
		try {
			return Integer.parseInt(integerLiteral);
		} catch (NumberFormatException e) {
			throw new SyntaxException("'" + integerLiteral + "' is not a valid integer literal");
		}
	}

	public long readLongLiteral() throws SyntaxException, CodeCompletionException {
		String longLiteral = readRegex(LONG_LITERAL_PATTERN, 2, NO_COMPLETIONS, "Expected a long literal", true);
		try {
			return Long.parseLong(longLiteral);
		} catch (NumberFormatException e) {
			throw new SyntaxException("'" + longLiteral + "' is not a valid long literal");
		}
	}

	public float readFloatLiteral() throws SyntaxException, CodeCompletionException {
		String floatLiteral = readRegex(FLOAT_LITERAL_PATTERN, 2, NO_COMPLETIONS, "Expected a float literal", true);
		try {
			return Float.parseFloat(floatLiteral);
		} catch (NumberFormatException e) {
			throw new SyntaxException("'" + floatLiteral + "' is not a valid float literal");
		}
	}

	public double readDoubleLiteral() throws SyntaxException, CodeCompletionException {
		String doubleLiteral = readRegex(DOUBLE_LITERAL_PATTERN, 2, NO_COMPLETIONS, "Expected a double literal", true);
		try {
			return Double.parseDouble(doubleLiteral);
		} catch (NumberFormatException e) {
			throw new SyntaxException("'" + doubleLiteral + "' is not a valid double literal");
		}
	}

	public char readCharacter(char... expectedCharacters) throws SyntaxException, CodeCompletionException, InternalErrorException {
		Pattern pattern = getOrCreateSingleCharacterPattern(expectedCharacters);
		String s = readRegex(pattern, 2, NO_COMPLETIONS, "Expected " + joinCharacters(expectedCharacters), false);
		if (s.length() > 1) {
			throw new InternalErrorException(toString() + ": Obtained " + s.length() + " characters when parsing only 1: " + s);
		}
		char c = s.isEmpty() ? EMPTY_CHARACTER : s.charAt(0);
		for (char expectedCharacter : expectedCharacters) {
			if (c == expectedCharacter) {
				return c;
			}
		}
		throw new InternalErrorException(toString() +  ": Expected " + joinCharacters(expectedCharacters) + ", but found character " + c);
	}

	public char peekCharacter() {
		String characters = peekCharacters();
		return characters.isEmpty() ? EMPTY_CHARACTER : characters.charAt(0);
	}

	public String peekCharacters() {
		Matcher matcher = match(CHARACTERS_PATTERN);
		if (!matcher.matches()) {
			throw new IllegalStateException("Internal Error: Characters pattern did not match");
		}
		return matcher.group(1);
	}

	/**
	 * Skips the next character if it is the specified character. Returns true in this case.
	 * Otherwise, the method does nothing and returns false.
	 */
	public boolean skipCharacter(char character) throws InternalErrorException, CodeCompletionException {
		if (peekCharacter() == character) {
			try {
				readCharacter(character);
				return true;
			} catch (SyntaxException e) {
				throw new InternalErrorException(toString() + ": Unexpected parse exception: " + e.getMessage());
			}
		}
		return false;
	}

	public String readUntilCharacter(CompletionGenerator completionGenerator, char... terminalCharacters) throws SyntaxException, CodeCompletionException {
		StringBuilder regex = new StringBuilder("^([^");
		for (char terminalCharacter : terminalCharacters) {
			regex.append(RegexUtils.escapeIfSpecial(terminalCharacter));
		}
		regex.append("]+).*");
		Pattern pattern = Pattern.compile(regex.toString());
		return readRegex(pattern, 1, completionGenerator, "Failed parsing until " + joinCharacters(terminalCharacters), true);
	}

	@Nullable
	public UnaryOperator readUnaryOperator() throws SyntaxException, CodeCompletionException {
		String operator = readOperator(UNARY_OPERATORS);
		return UnaryOperator.getValue(operator);
	}

	@Nullable
	public BinaryOperator readBinaryOperator() throws SyntaxException, CodeCompletionException {
		String operator = readOperator(BINARY_OPERATORS);
		return BinaryOperator.getValue(operator);
	}

	@Nullable
	public BinaryOperator peekBinaryOperator() throws SyntaxException, CodeCompletionException {
		int curPos = position;
		String operator = readOperator(BINARY_OPERATORS);
		position = curPos;
		return BinaryOperator.getValue(operator);
	}

	public String readRemainingWhitespaces(CompletionGenerator completionGenerator, String errorMessage) throws SyntaxException, CodeCompletionException {
		return readRegex(REMAINING_WHITESPACE_PATTERN, 1, completionGenerator, errorMessage, true);
	}

	private String readRegex(Pattern pattern, int groupIndexToExtract, CompletionGenerator completionGenerator, String errorMessage, boolean supportCompletionsInTrailingWhitespaces) throws SyntaxException, CodeCompletionException {
		if (caretPosition < position) {
			throw new IllegalStateException("Internal error: Reading tokens after caret position");
		}
		int startPos = position;
		skipSpaces();
		int textStartPos = position;

		if (caretPosition < position) {
			// Completion within leading white spaces
			CompletionInfo completionSuggestionInfo = new CompletionSuggestionInfoImpl(startPos, caretPosition, caretPosition, caretPosition);
			CodeCompletions completions = completionGenerator.generate(completionSuggestionInfo);
			throw new CodeCompletionException(completions);
		}

		Matcher matcher = match(pattern);
		if (!matcher.matches()) {
			if (caretPosition == textStartPos) {
				// Completion at beginning of non-white spaces
				CompletionInfo completionSuggestionInfo = new CompletionSuggestionInfoImpl(startPos, textStartPos, textStartPos, textStartPos);
				CodeCompletions completions = completionGenerator.generate(completionSuggestionInfo);
				throw new CodeCompletionException(completions);
			}
			// No completion requested, no match
			throw new SyntaxException(errorMessage);
		}

		String wholeToken = matcher.group(1);
		int endPos = textStartPos + wholeToken.length();
		String extractedString = matcher.group(groupIndexToExtract);
		int textEndPos = textStartPos + extractedString.length();

		if (caretPosition <= endPos) {
			// Code completion
			if (caretPosition < textEndPos || supportCompletionsInTrailingWhitespaces) {
				position = endPos;
				CompletionInfo completionSuggestionInfo = new CompletionSuggestionInfoImpl(startPos, endPos, textStartPos, textEndPos);
				CodeCompletions completions = completionGenerator.generate(completionSuggestionInfo);
				throw new CodeCompletionException(completions);
			}
			// Move to the beginning of the trailing white spaces, return non-white spaces and handle completions when reading next token
			position = textEndPos;
			assert caretPosition >= position;
		} else {
			// No code completion => move to the end of the parsed area and return non-white spaces
			position = endPos;
			assert caretPosition > position;
		}
		return extractedString;
	}

	@Nullable
	private String readOperator(List<String> availableOperators) throws SyntaxException, CodeCompletionException {
		if (caretPosition < position) {
			throw new IllegalStateException("Internal error: Reading operator after caret position");
		}
		skipSpaces();
		if (caretPosition < position) {
			// Completion within leading white spaces
			throw new CodeCompletionException(CodeCompletions.NONE);
		}
		int expressionLength = expression.length();
		String detectedOperator = null;
		boolean requestedCodeCompletionInsideOperator = false;
		for (String operator : availableOperators) {
			int endIndex = position + operator.length();
			if (endIndex <= expressionLength) {
				if (operator.equals(expression.substring(position, endIndex))) {
					detectedOperator = operator;
					requestedCodeCompletionInsideOperator = caretPosition < endIndex;
					break;
				}
			} else if (operator.startsWith(expression.substring(position))) {
				/*
				 * Possible situation: operator == "&&", code completion requested at the end of
				 * expression that ends with "&". We want to interpret this as operator "&" and
				 * code completion for next argument instead of requested code completion for "&&".
				 * => We cannot break here, but let the other branch, which is later executed for "&",
				 * set requestedCodeCompletionInsideOperator to false.
				 */
				requestedCodeCompletionInsideOperator |= (caretPosition <= expressionLength);
			}
		}

		if (requestedCodeCompletionInsideOperator) {
			// Do not provide completions for operators
			throw new CodeCompletionException(CodeCompletions.NONE);
		}
		if (detectedOperator == null) {
			return null;
		}
		position += detectedOperator.length();
		if (caretPosition < position) {
			throw new IllegalStateException("Internal error: Skipped caret position");
		}
		skipSpaces();
		if (position > caretPosition) {
			// We must not skip the caret
			position = caretPosition;
		}
		return detectedOperator;
	}

	private void skipSpaces() {
		Matcher matcher = match(WHITESPACE_PATTERN);
		if (!matcher.matches()) {
			throw new IllegalStateException("Internal Error: Optional space pattern did not match");
		}
		String leadingSpaces = matcher.group(1);
		position += leadingSpaces.length();
	}

	private String joinCharacters(char[] characters) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < characters.length; i++) {
			if (i > 0) {
				builder.append(", ");
				if (i == characters.length - 1) {
					builder.append(" or ");
				}
			}
			builder.append("'").append(characters[i]).append("'");
		}
		return builder.toString();
	}

	private Matcher match(Pattern pattern) {
		return pattern.matcher(expression.substring(position));
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	@Override
	public String toString() {
		return expression.substring(0, position)
				+ "^"
				+ expression.substring(position);
	}

	private class CompletionSuggestionInfoImpl implements CompletionInfo
	{
		private final int startPos;
		private final int endPos;
		private final int textStartPos;
		private final int textEndPos;

		private CompletionSuggestionInfoImpl(int startPos, int endPos, int textStartPos, int textEndPos) {
			this.startPos = startPos;
			this.endPos = endPos;
			this.textStartPos = textStartPos;
			this.textEndPos = textEndPos;
		}

		@Override
		public int getTokenStartPosition() {
			return startPos;
		}

		@Override
		public int getTokenEndPosition() {
			return endPos;
		}

		@Override
		public int getTokenTextStartPosition() {
			return textStartPos;
		}

		@Override
		public int getTokenTextEndPosition() {
			return textEndPos;
		}

		@Override
		public int getCaretPosition() {
			return caretPosition;
		}

		@Override
		public String getTokenText() {
			return expression.substring(textStartPos, textEndPos);
		}

		@Override
		public String getTokenTextUntilCaret() {
			return expression.substring(textStartPos, Math.min(textEndPos, caretPosition));
		}
	}
}

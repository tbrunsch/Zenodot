package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.matching.*;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.result.ParseError.ErrorPriority;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionKeyword;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.EvaluationMode;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

/**
 * Parses subexpressions {@code class} of expressions of the form {@code <class>.class}.
 * The class {@code <class>} is the context for the parser.
 */
public class ClassObjectParser extends AbstractParserWithObjectTail<TypeInfo>
{
	private static final String	CLASS_KEYWORD	= "class";

	public ClassObjectParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	ParseOutcome parseNext(TokenStream tokenStream, TypeInfo contextType, ParseExpectation expectation) {
		int startPosition = tokenStream.getPosition();
		Token keywordToken = tokenStream.readKeyWordUnchecked();
		if (keywordToken == null) {
			log(LogLevel.ERROR, "expected keyword '" + CLASS_KEYWORD + "'");
			return ParseOutcomes.createParseError(startPosition, "Expected keyword '" + CLASS_KEYWORD + "'", ErrorPriority.WRONG_PARSER);
		}

		if (keywordToken.isContainsCaret()) {
			if (CLASS_KEYWORD.startsWith(keywordToken.getValue())) {
				MatchRating rating = MatchRatings.create(StringMatch.PREFIX, TypeMatch.NONE, AccessMatch.IGNORED);
				CompletionSuggestion suggestion = new CompletionSuggestionKeyword(CLASS_KEYWORD, startPosition, tokenStream.getPosition());
				log(LogLevel.INFO, "suggesting keyword '" + CLASS_KEYWORD + "'...");
				return CompletionSuggestions.of(suggestion, rating);
			} else {
				log(LogLevel.INFO, "no completion suggestions available");
				return CompletionSuggestions.none(tokenStream.getPosition());
			}
		}

		if (!keywordToken.getValue().equals(CLASS_KEYWORD)) {
			log(LogLevel.ERROR, "expected keyword '" + CLASS_KEYWORD + "'");
			return ParseOutcomes.createParseError(startPosition, "Expected keyword '" + CLASS_KEYWORD + "'", ErrorPriority.WRONG_PARSER);
		}

		Class<?> classObject = contextType.getRawType();
		ObjectInfo classObjectInfo = InfoProvider.createObjectInfo(classObject, InfoProvider.createTypeInfo(Class.class));

		int position = tokenStream.getPosition();
		return isCompile()
				? compile(classObject, position, classObjectInfo)
				: ParseOutcomes.createObjectParseResult(position, classObjectInfo);
	}

	private ParseOutcome compile(Class<?> classObject, int position, ObjectInfo classObjectInfo) {
		return new CompiledClassObjectParseResult(classObject, position, classObjectInfo);
	}

	private static class CompiledClassObjectParseResult extends AbstractCompiledParseResult
	{
		private final Class<?>	classObject;

		CompiledClassObjectParseResult(Class<?> classObject, int position, ObjectInfo classObjectInfo) {
			super(position, classObjectInfo);
			this.classObject = classObject;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo contextInfo) throws Exception {
			return InfoProvider.createObjectInfo(classObject, InfoProvider.createTypeInfo(Class.class));
		}
	}
}

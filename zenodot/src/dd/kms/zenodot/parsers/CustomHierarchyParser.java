package dd.kms.zenodot.parsers;

import com.google.common.collect.Iterables;
import dd.kms.zenodot.common.RegexUtils;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.result.ParseError.ErrorPriority;
import dd.kms.zenodot.settings.ObjectTreeNode;
import dd.kms.zenodot.settings.ParserSettingsBuilder;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.EvaluationMode;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import java.util.regex.Pattern;

/**
 * Parses expressions of the form {@code {<child level 1>#...#<child level n>}} in
 * the (ignored) context of {@code this}. {@code <child level 1>} refers to a child
 * of the root of the custom hierarchy specified by {@link ParserSettingsBuilder#customHierarchyRoot(ObjectTreeNode)}.
 * With each separator {@code #}, the expression descends to the next lower level
 * in the hierarchy.
 */
public class CustomHierarchyParser extends AbstractParserWithObjectTail<ObjectInfo>
{
	private static final char		HIERARCHY_BEGIN		= '{';
	private static final char		HIERARCHY_SEPARATOR	= '#';
	private static final char		HIERARCHY_END		= '}';

	private static final Pattern	HIERARCHY_NODE_PATTERN	= Pattern.compile("^([^" + RegexUtils.escapeIfSpecial(HIERARCHY_SEPARATOR) + RegexUtils.escapeIfSpecial(HIERARCHY_END) + "]*).*");

	public CustomHierarchyParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	ParseOutcome parseNext(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		if (tokenStream.isCaretWithinNextWhiteSpaces()) {
			return CompletionSuggestions.none(tokenStream.getPosition());
		}

		int position = tokenStream.getPosition();
		Token characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null || characterToken.getValue().charAt(0) != HIERARCHY_BEGIN) {
			tokenStream.moveTo(position);
			log(LogLevel.ERROR, "missing '" + HIERARCHY_BEGIN + "' at " + tokenStream);
			return ParseOutcomes.createParseError(position, "Expected hierarchy begin character ('" + HIERARCHY_BEGIN + "')", ErrorPriority.WRONG_PARSER);
		}

		ObjectTreeNode hierarchyNode = parserToolbox.getSettings().getCustomHierarchyRoot();
		ParseOutcome parseOutcome = parseHierarchyNode(tokenStream, hierarchyNode, expectation);
		return parseOutcome;
	}

	private ParseOutcome parseHierarchyNode(TokenStream tokenStream, ObjectTreeNode contextNode, ParseExpectation expectation) {
		int startPosition = tokenStream.getPosition();
		if (tokenStream.isCaretWithinNextWhiteSpaces()) {
			log(LogLevel.INFO, "suggesting custom hierarchy nodes for completion...");
			return parserToolbox.getObjectTreeNodeDataProvider().suggestNodes("", contextNode, startPosition, startPosition);
		}

		Token nodeToken = tokenStream.readRegexUnchecked(HIERARCHY_NODE_PATTERN, 1);
		if (nodeToken == null) {
			log(LogLevel.ERROR, "missing hierarchy node name at " + tokenStream);
			return ParseOutcomes.createParseError(startPosition, "Expected a hierarchy node name", ErrorPriority.WRONG_PARSER);
		}
		String nodeName = nodeToken.getValue();
		int endPosition = tokenStream.getPosition();

		// check for code completion
		if (nodeToken.isContainsCaret()) {
			log(LogLevel.SUCCESS, "suggesting hierarchy nodes matching '" + nodeName + "'");
			return parserToolbox.getObjectTreeNodeDataProvider().suggestNodes(nodeName, contextNode, startPosition, endPosition);
		}

		Iterable<? extends ObjectTreeNode> childNodes = contextNode.getChildNodes();
		ObjectTreeNode firstChildNodeMatch = Iterables.getFirst(Iterables.filter(childNodes, node -> node.getName().equals(nodeName)), null);
		if (firstChildNodeMatch == null) {
			log(LogLevel.ERROR, "unknown hierarchy node '" + nodeName + "'");
			return ParseOutcomes.createParseError(startPosition, "Unknown hierarchy node '" + nodeName + "'", ErrorPriority.RIGHT_PARSER);
		}
		log(LogLevel.SUCCESS, "detected hierarchy node '" + nodeName + "'");

		Token characterToken = tokenStream.readCharacterUnchecked();
		char character = characterToken == null ? (char) 0 : characterToken.toString().charAt(0);

		if (character == HIERARCHY_SEPARATOR) {
			return parseHierarchyNode(tokenStream, firstChildNodeMatch, expectation);
		} else if (character == HIERARCHY_END) {
			ObjectInfo userObject = firstChildNodeMatch.getUserObject();
			int position = tokenStream.getPosition();
			return isCompile()
					? ParseOutcomes.createCompiledConstantObjectParseResult(position, userObject)
					: ParseOutcomes.createObjectParseResult(position, userObject);
		}

		log(LogLevel.ERROR, "expected '" + HIERARCHY_SEPARATOR + "' or '" + HIERARCHY_END + "'");
		return ParseOutcomes.createParseError(endPosition, "Expected hierarchy separator ('" + HIERARCHY_SEPARATOR + "') or hierarchy end character ('" + HIERARCHY_END + "')", ErrorPriority.RIGHT_PARSER);
	}

	private ParseOutcome compile(ParseOutcome tailParseOutcome, ObjectInfo userObjectInfo) {
		if (!ParseOutcomes.isCompiledParseResult(tailParseOutcome)) {
			return tailParseOutcome;
		}
		CompiledObjectParseResult compiledTailParseResult = (CompiledObjectParseResult) tailParseOutcome;
		return new CompiledCustomHierarchyParseResult(compiledTailParseResult, userObjectInfo);
	}

	private static class CompiledCustomHierarchyParseResult extends AbstractCompiledParseResult
	{
		private final CompiledObjectParseResult	compiledTailParseResult;
		private final ObjectInfo							userObjectInfo;

		CompiledCustomHierarchyParseResult(CompiledObjectParseResult compiledTailParseResult, ObjectInfo userObjectInfo) {
			super(compiledTailParseResult.getPosition(), compiledTailParseResult.getObjectInfo());
			this.compiledTailParseResult = compiledTailParseResult;
			this.userObjectInfo = userObjectInfo;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo contextInfo) throws Exception {
			return compiledTailParseResult.evaluate(thisInfo, userObjectInfo);
		}
	}
}

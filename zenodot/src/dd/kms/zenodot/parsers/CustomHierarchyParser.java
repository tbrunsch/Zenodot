package dd.kms.zenodot.parsers;

import com.google.common.collect.Iterables;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.flowcontrol.InternalErrorException;
import dd.kms.zenodot.flowcontrol.InternalParseException;
import dd.kms.zenodot.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.result.CodeCompletions;
import dd.kms.zenodot.result.ObjectParseResult;
import dd.kms.zenodot.result.ParseResults;
import dd.kms.zenodot.settings.ObjectTreeNode;
import dd.kms.zenodot.settings.ParserSettingsBuilder;
import dd.kms.zenodot.tokenizer.CompletionInfo;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.dataproviders.ObjectTreeNodeDataProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import java.util.Objects;

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

	public CustomHierarchyParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	ObjectParseResult parseNext(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws InternalParseException, CodeCompletionException, InternalErrorException {
		tokenStream.readCharacter(HIERARCHY_BEGIN);

		increaseConfidence(ParserConfidence.POTENTIALLY_RIGHT_PARSER);

		ObjectTreeNode hierarchyNode = parserToolbox.getSettings().getCustomHierarchyRoot();
		return parseHierarchyNode(tokenStream, hierarchyNode, expectation);
	}

	private ObjectParseResult parseHierarchyNode(TokenStream tokenStream, ObjectTreeNode contextNode, ObjectParseResultExpectation expectation) throws InternalParseException, CodeCompletionException, InternalErrorException {
		String nodeName = tokenStream.readUntilCharacter(info -> suggestHierarchyNode(contextNode, expectation, info), HIERARCHY_SEPARATOR, HIERARCHY_END);

		Iterable<? extends ObjectTreeNode> childNodes = contextNode.getChildNodes();
		ObjectTreeNode firstChildNodeMatch = Iterables.getFirst(Iterables.filter(childNodes, node -> Objects.equals(node.getName(), nodeName)), null);
		if (firstChildNodeMatch == null) {
			throw new InternalParseException("Unknown hierarchy node '" + nodeName + "'");
		}
		log(LogLevel.SUCCESS, "detected hierarchy node '" + nodeName + "'");
		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		char nextChar = tokenStream.readCharacter(HIERARCHY_SEPARATOR, HIERARCHY_END);
		switch (nextChar) {
			case HIERARCHY_SEPARATOR:
				return parseHierarchyNode(tokenStream, firstChildNodeMatch, expectation);
			case HIERARCHY_END: {
				ObjectInfo userObject = firstChildNodeMatch.getUserObject();
				return isCompile()
					? ParseResults.createCompiledConstantObjectParseResult(userObject)
					: ParseResults.createObjectParseResult(userObject);
			}
			default:
				throw new InternalErrorException(tokenStream.toString() + ": Expected '" + HIERARCHY_SEPARATOR + "' or '" + HIERARCHY_END + "', but found '" + nextChar + "'");
		}
	}

	private CodeCompletions suggestHierarchyNode(ObjectTreeNode contextNode, ObjectParseResultExpectation expectation, CompletionInfo info) {
		int insertionBegin = getInsertionBegin(info);
		int insertionEnd = getInsertionEnd(info);
		String nameToComplete = getTextToComplete(info);

		log(LogLevel.SUCCESS, "suggesting hierarchy nodes matching '" + nameToComplete + "'");

		ObjectTreeNodeDataProvider objectTreeNodeDataProvider = parserToolbox.getObjectTreeNodeDataProvider();
		return objectTreeNodeDataProvider.completeNode(nameToComplete, contextNode, expectation, insertionBegin, insertionEnd);
	}
}

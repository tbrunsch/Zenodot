package dd.kms.zenodot.impl.parsers;

import com.google.common.collect.Iterables;
import dd.kms.zenodot.api.CustomHierarchyParsers;
import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.api.settings.CustomHierarchyParserSettings;
import dd.kms.zenodot.api.settings.ObjectTreeNode;
import dd.kms.zenodot.framework.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.framework.flowcontrol.InternalErrorException;
import dd.kms.zenodot.framework.flowcontrol.SyntaxException;
import dd.kms.zenodot.framework.parsers.AbstractParserWithObjectTail;
import dd.kms.zenodot.framework.parsers.ParserConfidence;
import dd.kms.zenodot.framework.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.framework.result.CodeCompletions;
import dd.kms.zenodot.framework.result.ObjectParseResult;
import dd.kms.zenodot.framework.result.ParseResults;
import dd.kms.zenodot.framework.tokenizer.CompletionInfo;
import dd.kms.zenodot.framework.tokenizer.TokenStream;
import dd.kms.zenodot.framework.utils.ParseUtils;
import dd.kms.zenodot.framework.utils.ParserToolbox;
import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodot.impl.settings.parsers.AdditionalCustomHierarchyParserSettings;
import dd.kms.zenodot.impl.utils.dataproviders.ObjectTreeNodeDataProvider;

import java.util.Objects;

/**
 * Parses expressions of the form {@code {<child level 1>#...#<child level n>}} in
 * the (ignored) context of {@code this}. {@code <child level 1>} refers to a child
 * of the root of the custom hierarchy specified by {@link CustomHierarchyParsers#createCustomHierarchyParserSettings(ObjectTreeNode)}.
 * With each separator {@code #}, the expression descends to the next lower level
 * in the hierarchy.
 */
public class CustomHierarchyParser extends AbstractParserWithObjectTail<ObjectInfo>
{
	public CustomHierarchyParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	protected ObjectParseResult parseNext(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws SyntaxException, CodeCompletionException, InternalErrorException {
		CustomHierarchyParserSettings settings = ParseUtils.getAdditionalParserSettings(parserToolbox.getSettings(), AdditionalCustomHierarchyParserSettings.class).getSettings();

		tokenStream.readCharacter(settings.getHierarchyBegin());

		increaseConfidence(ParserConfidence.POTENTIALLY_RIGHT_PARSER);

		return parseHierarchyNode(tokenStream, settings, settings.getRoot(), expectation);
	}

	private ObjectParseResult parseHierarchyNode(TokenStream tokenStream, CustomHierarchyParserSettings settings, ObjectTreeNode contextNode, ObjectParseResultExpectation expectation) throws SyntaxException, CodeCompletionException, InternalErrorException {
		char hierarchySeparator = settings.getHierarchySeparator();
		char hierarchyEnd = settings.getHierarchyEnd();

		String nodeName = tokenStream.readUntilCharacter(info -> suggestHierarchyNode(contextNode, expectation, info), hierarchySeparator, hierarchyEnd);

		Iterable<? extends ObjectTreeNode> childNodes = contextNode.getChildNodes();
		ObjectTreeNode firstChildNodeMatch = Iterables.getFirst(Iterables.filter(childNodes, node -> Objects.equals(node.getName(), nodeName)), null);
		if (firstChildNodeMatch == null) {
			throw new SyntaxException("Unknown hierarchy node '" + nodeName + "'");
		}
		log(LogLevel.SUCCESS, "detected hierarchy node '" + nodeName + "'");
		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		char nextChar = tokenStream.readCharacter(hierarchySeparator, hierarchyEnd);
		if (nextChar == hierarchySeparator) {
			return parseHierarchyNode(tokenStream, settings, firstChildNodeMatch, expectation);
		} else if (nextChar == hierarchyEnd) {
			Object userObject = firstChildNodeMatch.getUserObject();
			return ParseResults.createCompiledConstantObjectParseResult(InfoProvider.createObjectInfo(userObject), tokenStream);

		}
		throw new InternalErrorException(tokenStream.toString() + ": Expected '" + hierarchySeparator + "' or '" + hierarchyEnd + "', but found '" + nextChar + "'");
	}

	private CodeCompletions suggestHierarchyNode(ObjectTreeNode contextNode, ObjectParseResultExpectation expectation, CompletionInfo info) {
		int insertionBegin = getInsertionBegin(info);
		int insertionEnd = getInsertionEnd(info);
		String nameToComplete = getTextToComplete(info);

		log(LogLevel.SUCCESS, "suggesting hierarchy nodes matching '" + nameToComplete + "'");

		ObjectTreeNodeDataProvider objectTreeNodeDataProvider = parserToolbox.inject(ObjectTreeNodeDataProvider.class);
		return objectTreeNodeDataProvider.completeNode(nameToComplete, contextNode, expectation, insertionBegin, insertionEnd);
	}
}

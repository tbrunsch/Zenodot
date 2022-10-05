package dd.kms.zenodot.samples;

import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.ObjectTreeNode;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import dd.kms.zenodot.api.settings.ParserSettingsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This sample demonstrates how to represent a custom hierarchy using {@link ObjectTreeNode}s
 * in order to make it accessible by Zenodot. The hierarchy we want to represent is:
 *
 * <ul>
 *     <li>
 *         numbers
 *         <ul>
 *             <li>pi = 3.14</li>
 *             <li>e = 2.72</li>
 *         </ul>
 *     </li>
 *     <li>
 *         strings
 *         <ul>
 *             <li>
 *                 short strings
 *                 <ul>
 *                     <li>test = "Test"</li>
 *                 </ul>
 *             </li>
 *             <li>
 *                 long strings
 *                 <ul>
 *                     <li>long string = "This is a long string."</li>
 *                     <li>very long string = "This is a very long string."</li>
 *                 </ul>
 *             </li>
 *         </ul>
 *     </li>
 * </ul>
 */
public class CustomHierarchySample
{
	public static void main(String[] args) throws ParseException {
		ObjectTreeNode root = node(null,
			node("numbers",
				leaf("pi", 3.14),
				leaf("e", 2.72)),
			node("strings",
				node("short strings",
					leaf("test", "Test")),
				node("long strings",
					leaf("long string", "This is a long string."),
					leaf("very long string", "This is a very long string.")
				)
			)
		);

		ParserSettings settings = ParserSettingsBuilder.create()
			.customHierarchyRoot(root)
			.build();
		ExpressionParser parser = Parsers.createExpressionParser(settings);
		String text = "{strings#long strings#ve";
		List<CodeCompletion> completions = new ArrayList<>(parser.getCompletions(text, text.length(), null));
		Collections.sort(completions, Parsers.COMPLETION_COMPARATOR);

		System.out.println("Completion: " + completions.get(0).getTextToInsert());

		String expression = "{numbers#e}";
		Object result = parser.evaluate(expression, null);

		System.out.println("Result: " + result);
	}

	private static ObjectTreeNode node(String name, ObjectTreeNode... childNodes) {
		return new ObjectTreeNode() {
			@Override
			public String getName() {
				return name;
			}

			@Override
			public Iterable<? extends ObjectTreeNode> getChildNodes() {
				return Arrays.asList(childNodes);
			}

			@Override
			public Object getUserObject() {
				return null;
			}
		};
	}

	private static ObjectTreeNode leaf(String name, Object value) {
		return ParserSettingsUtils.createLeafNode(name, value);
	}
}

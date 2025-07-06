package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumericLiteralRule<T> extends AbstractRule<Void, InstanceParseResult, JavaSettings> implements SimpleRule<Void, InstanceParseResult, JavaSettings>
{
	private final Class<T>				literalClass;
	private final Pattern				pattern;
	private final int[]					groupsToConsiderForExtraction;
	private final Function<String, T>	parser;

	public NumericLiteralRule(Class<T> literalClass, String literalRegex, Function<String, T> parser, int... groupsToConsiderForExtraction) {
		this.literalClass = literalClass;
		this.pattern = Pattern.compile(literalRegex);
		this.groupsToConsiderForExtraction = groupsToConsiderForExtraction;
		this.parser = parser;
		name(literalClass.getSimpleName() + " literal");
	}

	@Override
	public SyntaxRule getSyntaxRule() {
		return new SyntaxRule() {
			@Override
			public Pattern getRegex() {
				return pattern;
			}

			@Override
			public String getSyntaxDescription() {
				return "Literal of type \"" + literalClass.getSimpleName() + "\"";
			}
		};
	}

	@Override
	public SemanticRule<Void, InstanceParseResult, JavaSettings> getSemanticRule() {
		return new AbstractSemanticJavaRule<Void, InstanceParseResult>()
		{
			@Override
			void doSuggestCodeCompletions(Void input, String parsedString, JavaSettings settings) {
				/* nothing to do */
			}

			@Override
			void doSuggestMethodParameters(Void input, JavaSettings settings) {
				/* nothing to do */
			}

			@Override
			public InstanceParseResult evaluate(Void input, String parsedString, JavaSettings settings) throws SemanticException, EvaluationException {
				String valueAsString = extractValueAsString(parsedString);
				T value;
				try {
					value = parser.apply(valueAsString);
				} catch (NumberFormatException e) {
					throw new SemanticException("\"" + parsedString + "\" is no valid " + literalClass.getSimpleName() + " literal");
				}
				ObjectInfo valueInfo = InfoProvider.createObjectInfo(value, literalClass);
				return new ConstantParseResult(valueInfo);
			}

			private String extractValueAsString(String parsedString) {
				Matcher matcher = pattern.matcher(parsedString);
				if (!matcher.matches()) {
					throw new IllegalStateException("Syntax rule for literal contradicts regex pattern: \"" + parsedString + "\" cannot be parsed");
				}
				String extractedValueString = null;
				for (int groupIndex : groupsToConsiderForExtraction) {
					String groupString = matcher.group(groupIndex);
					if (groupString == null) {
						continue;
					}
					if (extractedValueString != null) {
						throw new IllegalStateException("Found ambiguity when extracting literal value from \"" + parsedString + "\": Multiple groups contain relevant data about the literal: \"" + extractedValueString + "\" vs. \"" + groupString + "\"");
					}
					extractedValueString = groupString;
				}
				if (extractedValueString == null) {
					throw new IllegalStateException("Could not extract literal data from \"" + parsedString + "\"");
				}
				return extractedValueString;
			}
		};
	}
}

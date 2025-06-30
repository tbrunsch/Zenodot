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
import java.util.regex.Pattern;

public class NumericLiteralRule<T> extends AbstractRule<Void, InstanceParseResult, JavaSettings> implements SimpleRule<Void, InstanceParseResult, JavaSettings>
{
	private final Class<T>				literalClass;
	private final Pattern				pattern;
	private final Function<String, T>	parser;

	public NumericLiteralRule(Class<T> literalClass, String literalRegex, Function<String, T> parser) {
		this.literalClass = literalClass;
		this.pattern = Pattern.compile(literalRegex);
		this.parser = parser;
		name(literalClass.getSimpleName() + " literal");
	}

	@Override
	public SyntaxRule getSyntaxRule() {
		return () -> pattern;
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
				T value;
				try {
					value = parser.apply(parsedString);
				} catch (NumberFormatException e) {
					throw new SemanticException("\"" + parsedString + "\" is no valid " + literalClass.getSimpleName() + " literal");
				}
				ObjectInfo valueInfo = InfoProvider.createObjectInfo(value, literalClass);
				return new ConstantParseResult(valueInfo);
			}
		};
	}
}

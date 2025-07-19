package dd.kms.zenodotx.java.rule.operator.unary;

import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.rule.AbstractSemanticJavaRule;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;
import dd.kms.zenodotx.rule.simple.SyntaxRules;

import java.util.Collection;
import java.util.regex.Pattern;

public class UnaryPrefixOperatorParseRule extends AbstractRule<Void, String, JavaSettings> implements SimpleRule<Void, String, JavaSettings>
{
	private final UnaryOperatorRegistry registry;

	public UnaryPrefixOperatorParseRule(UnaryOperatorRegistry registry) {
		this.registry = registry;
	}

	@Override
	public SyntaxRule getSyntaxRule() {
		Collection<String> operators = registry.getOperators();
		Pattern pattern = SyntaxRules.getPatternForStringAlternatives(operators);

		// TODO: Cache
		return new SyntaxRule() {
			@Override
			public Pattern getRegex() {
				return pattern;
			}

			@Override
			public String getSyntaxDescription() {
				return "Unary prefix operator";
			}
		};
	}

	@Override
	public SemanticRule<Void, String, JavaSettings> getSemanticRule() {
		return new AbstractSemanticJavaRule<Void, String>() {
			@Override
			protected void doSuggestCodeCompletions(Void input, String parsedString, JavaSettings settings) {
				// TODO
			}

			@Override
			protected void doSuggestMethodParameters(Void input, JavaSettings settings) {
				/* nothing to do */
			}

			@Override
			public String evaluate(Void input, String prefixOperator, JavaSettings settings) throws SemanticException, EvaluationException {
				return prefixOperator;
			}
		};
	}
}

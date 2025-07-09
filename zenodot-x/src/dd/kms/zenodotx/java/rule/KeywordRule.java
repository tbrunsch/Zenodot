package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;
import dd.kms.zenodotx.rule.simple.SyntaxRules;

import java.util.regex.Pattern;

public class KeywordRule<IO> extends AbstractRule<IO, IO, JavaSettings> implements SimpleRule<IO, IO, JavaSettings>
{
	private final String	keyword;

	public KeywordRule(String keyword) {
		this.keyword = keyword;
	}

	@Override
	public SyntaxRule getSyntaxRule() {
		Pattern pattern = SyntaxRules.getPatternForString(keyword);
		return new SyntaxRule() {
			@Override
			public Pattern getRegex() {
				return pattern;
			}

			@Override
			public String getSyntaxDescription() {
				return "\"" + keyword + "\"";
			}
		};
	}

	@Override
	public SemanticRule<IO, IO, JavaSettings> getSemanticRule() {
		return new AbstractSemanticJavaRule<IO, IO>() {
			@Override
			public IO evaluate(IO input, String parsedString, JavaSettings settings) throws SemanticException, EvaluationException {
				return input;
			}

			@Override
			void doSuggestCodeCompletions(IO input, String parsedString, JavaSettings settings) {
				// TODO
			}

			@Override
			void doSuggestMethodParameters(IO input, JavaSettings settings) {
				/* nothing to do */
			}
		};
	}

	@Override
	protected String getGenericName() {
		return keyword;
	}
}

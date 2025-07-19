package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;

import java.util.regex.Pattern;

public class ThisRule extends AbstractRule<Void, InstanceParseResult, JavaSettings> implements SimpleRule<Void, InstanceParseResult, JavaSettings>
{
	private static final Pattern	THIS_PATTERN	= Pattern.compile("this");

	public ThisRule() {
		name("this");
	}

	@Override
	public SyntaxRule getSyntaxRule() {
		return new SyntaxRule() {
			@Override
			public Pattern getRegex() {
				return THIS_PATTERN;
			}

			@Override
			public String getSyntaxDescription() {
				return "this";
			}
		};
	}

	@Override
	public SemanticRule<Void, InstanceParseResult, JavaSettings> getSemanticRule() {
		return new AbstractSemanticJavaRule<Void, InstanceParseResult>() {
			@Override
			protected void doSuggestCodeCompletions(Void input, String parsedString, JavaSettings settings) {
				/* TODO: Suggest "this" */
			}

			@Override
			protected void doSuggestMethodParameters(Void input, JavaSettings settings) {
				/* TODO */
			}

			@Override
			public InstanceParseResult evaluate(Void input, String parsedString, JavaSettings settings) {
				return new ThisParseResult(settings.getThisInfo());
			}
		};
	}
}

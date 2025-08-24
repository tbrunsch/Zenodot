package dd.kms.zenodotx.java.rule.literal;

import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.java.rule.AbstractSemanticJavaRule;
import dd.kms.zenodotx.java.rule.ConstantParseResult;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;

import java.util.regex.Pattern;

public class KeywordLiteralRule extends AbstractRule<Void, InstanceParseResult, JavaSettings> implements SimpleRule<Void, InstanceParseResult, JavaSettings>
{
	private final String				keyword;
	private final Pattern				pattern;
	private final InstanceParseResult	constantParseRsult;

	public KeywordLiteralRule(String keyword, ObjectInfo objectInfo) {
		this.keyword = keyword;
		this.pattern = Pattern.compile(keyword);
		this.constantParseRsult = new ConstantParseResult(objectInfo);
		name(keyword);
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
				return "Keyword \"" + keyword + "\"";
			}
		};
	}

	@Override
	public SemanticRule<Void, InstanceParseResult, JavaSettings> getSemanticRule() {
		return new AbstractSemanticJavaRule<Void, InstanceParseResult>() {
			@Override
			protected void doSuggestCodeCompletions(Void input, String parsedString, JavaSettings settings) {
				/* TODO: Return keyword */
			}

			@Override
			protected void doSuggestMethodParameters(Void input, JavaSettings settings) {
				/* TODO */
			}

			@Override
			public InstanceParseResult evaluate(Void input, String parsedString, JavaSettings settings) {
				return constantParseRsult;
			}
		};
	}
}

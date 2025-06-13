package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.event.Event;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;

import java.util.regex.Pattern;

public class EndOfInputRule<IO> extends AbstractRule<IO, IO, JavaSettings> implements SimpleRule<IO, IO, JavaSettings>
{
	@Override
	public SyntaxRule getSyntaxRule() {
		Pattern pattern = Pattern.compile("\\s*$");
		return () -> pattern;
	}

	@Override
	public SemanticRule<IO, IO, JavaSettings> getSemanticRule() {
		return new SemanticRule<IO, IO, JavaSettings>() {
			@Override
			public IO evaluate(IO input, String parsedString, JavaSettings settings) {
				return input;
			}

			@Override
			public void handleEvent(Event event, IO input, String parsedString, JavaSettings settings) {
				/* nothing to do */
			}
		};
	}

	@Override
	protected String getGenericName() {
		return "end of input";
	}
}

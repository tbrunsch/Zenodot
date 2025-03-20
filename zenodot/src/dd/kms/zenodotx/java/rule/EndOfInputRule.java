package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.event.Event;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaState;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;

import java.util.regex.Pattern;

public class EndOfInputRule implements SimpleRule<JavaState>
{
	@Override
	public SyntaxRule getSyntaxRule() {
		Pattern pattern = Pattern.compile("\\s*$");
		return () -> pattern;
	}

	@Override
	public SemanticRule<JavaState> getSemanticRule() {
		return new SemanticRule<JavaState>() {
			@Override
			public void evaluate(String parsedString, JavaState state) throws SemanticException, EvaluationException {
				/* nothing to do */
			}

			@Override
			public void handleEvent(Event event, String parsedString, JavaState state) {
				/* nothing to do */
			}
		};
	}
}

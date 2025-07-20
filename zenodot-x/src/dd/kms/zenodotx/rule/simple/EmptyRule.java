package dd.kms.zenodotx.rule.simple;

import dd.kms.zenodotx.GrammarSettings;
import dd.kms.zenodotx.event.Event;
import dd.kms.zenodotx.rule.AbstractRule;

import java.util.regex.Pattern;

public class EmptyRule<IO, S extends GrammarSettings> extends AbstractRule<IO, IO, S> implements SimpleRule<IO, IO, S>
{
	private static final Pattern	EMPTY_PATTERN		= Pattern.compile("");
	private static final SyntaxRule	EMPTY_SYNTAX_RULE	= new SyntaxRule() {
																@Override
																public Pattern getRegex() {
																	return EMPTY_PATTERN;
																}

																@Override
																public String getSyntaxDescription() {
																	return "Empty string";
																}
															};

	@Override
	public SyntaxRule getSyntaxRule() {
		return EMPTY_SYNTAX_RULE;
	}

	@Override
	public SemanticRule<IO, IO, S> getSemanticRule() {
		return new SemanticRule<IO, IO, S>() {
			@Override
			public IO evaluate(IO input, String parsedString, S settings) {
				return input;
			}

			@Override
			public void handleEvent(Event event, IO input, String parsedString, S state) {
				/* don't do anything */
			}
		};
	}

	@Override
	protected String getGenericName() {
		return "empty";
	}
}

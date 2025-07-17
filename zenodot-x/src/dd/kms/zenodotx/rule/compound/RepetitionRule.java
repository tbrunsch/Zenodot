package dd.kms.zenodotx.rule.compound;

import dd.kms.zenodotx.GrammarSettings;
import dd.kms.zenodotx.Parser;
import dd.kms.zenodotx.ParserState;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.Rule;

public class RepetitionRule<IO, S extends GrammarSettings> extends AbstractRule<IO, IO, S> implements CompoundRule<IO, IO, S>
{
	private final Rule<IO, IO, S> ruleToRepeat;

	public RepetitionRule(Rule<IO, IO, S> ruleToRepeat) {
		this.ruleToRepeat = ruleToRepeat;
	}

	@Override
	public IO parse(IO input, S settings, Parser<S> parser) throws SyntaxException, EvaluationException, SemanticException, Parser.EventResultException {
		IO output = input;
		while (true) {
			ParserState stateBeforeNextRepetition = parser.getParserState();
			try {
				output = parser.parse(ruleToRepeat, output, settings);
			} catch (SyntaxException e) {
				parser.setParserState(stateBeforeNextRepetition);
				return output;
			}
		}
	}

	@Override
	public void parseSyntactically(Parser<S> parser) throws SyntaxException {
		while (true) {
			ParserState stateBeforeNextRepetition = parser.getParserState();
			try {
				parser.parseSyntactically(ruleToRepeat);
			} catch (SyntaxException e) {
				parser.setParserState(stateBeforeNextRepetition);
				return;
			}
		}
	}

	@Override
	protected String getGenericName() {
		return "(rule)*";
	}
}

package dd.kms.zenodotx.rule.compound;

import dd.kms.zenodotx.Parser;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.Rule;

public class RepetitionRule<IO, S> extends AbstractRule<IO, IO, S> implements CompoundRule<IO, IO, S>
{
	private final Rule<IO, IO, S> ruleToRepeat;

	public RepetitionRule(Rule<IO, IO, S> ruleToRepeat) {
		this.ruleToRepeat = ruleToRepeat;
	}

	@Override
	public IO parse(IO input, S settings, Parser<S> parser) throws SyntaxException, EvaluationException, SemanticException, Parser.EventResultException {
		IO output = input;
		while (true) {
			parser.storeState();
			try {
				output = parser.parse(ruleToRepeat, output, settings);
			} catch (SyntaxException e) {
				parser.restoreState();
				return output;
			} catch (SemanticException e) {
				parser.restoreState();
				throw e;
			}
		}
	}

	@Override
	protected String getGenericName() {
		return "(rule)*";
	}
}

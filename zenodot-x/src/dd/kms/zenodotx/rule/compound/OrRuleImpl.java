package dd.kms.zenodotx.rule.compound;

import dd.kms.zenodotx.Parser;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.Rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OrRuleImpl<I, O, S> extends AbstractRule<I, O, S> implements OrRule<I, O, S>
{
	private List<Rule<I, O, S>> 	alternatives	= new ArrayList<>();

	@Override
	public OrRule<I, O, S> name(String name) {
		super.name(name);
		return this;
	}

	@Override
	public O parse(I input, S settings, Parser<S> parser) throws SyntaxException, EvaluationException, SemanticException, Parser.EventResultException {
		List<SyntaxException> syntaxExceptions = new ArrayList<>();
		List<SemanticException> semanticExceptions = new ArrayList<>();
		List<Parser.EventResultException> eventResultExceptions = new ArrayList<>();
		for (Rule<I, O, S> alternative : alternatives) {
			parser.storeState();
			try {
				return parser.parse(alternative, input, settings);
			} catch (SyntaxException e) {
				// continue with next alternative
				syntaxExceptions.add(e);
			} catch (SemanticException e) {
				// continue with next alternative
				semanticExceptions.add(e);
			} catch (Parser.EventResultException e) {
				// continue with next alternative
				eventResultExceptions.add(e);
			}
			/*
			 * An EvaluationException is directly propagated to the caller because it means that we have found
			 * the correct alternative.
			 */
			parser.restoreState();
		}
		if (!eventResultExceptions.isEmpty()) {
			// TODO: Merge event result exceptions
			throw eventResultExceptions.get(0);
		} else if (!semanticExceptions.isEmpty()) {
			// TODO: Merge them
			throw semanticExceptions.get(0);
		} else if (!syntaxExceptions.isEmpty()) {
			// TODO: Merge them
			throw syntaxExceptions.get(0);
		} else {
			throw new IllegalStateException("Or rule does not have any alternatives");
		}
	}


	@Override
	public void setAlternatives(Rule<I, O, S>... rules) {
		setAlternatives(Arrays.asList(rules));
	}

	@Override
	public void setAlternatives(List<Rule<I, O, S>> rules) {
		alternatives = new ArrayList<>(rules);
	}

	@Override
	public List<Rule<I, O, S>> getAlternatives() {
		return alternatives;
	}

	@Override
	protected String getGenericName() {
		return "rule 1 or rule 2 or ...";
	}
}

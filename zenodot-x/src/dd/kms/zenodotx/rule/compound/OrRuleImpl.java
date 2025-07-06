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
import java.util.function.IntSupplier;

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
		if (alternatives.isEmpty()) {
			throw new SyntaxException("No alternatives have been defined for this or rule");
		}
		ResultAggregator<O> resultAggregator = new ResultAggregator<>();
		for (Rule<I, O, S> alternative : alternatives) {
			parser.storeState();
			try {
				O result = parser.parse(alternative, input, settings);
				resultAggregator.addResult(result, parser.getParsePosition());
			} catch (SyntaxException e) {
				resultAggregator.addSyntaxException(e, parser.getParsePosition());
			} catch (SemanticException e) {
				resultAggregator.addSemanticException(e, () -> {
					parser.restoreState();
					parser.storeState();
					try {
						parser.parseSyntactically(alternative);
					} catch (SyntaxException ignored) {
						/* does not matter */
					}
					return parser.getParsePosition();
				});
			} catch (Parser.EventResultException e) {
				resultAggregator.addEventResultException(e);
			}
			/*
			 * There is no catch for EvaluationExceptions because such Exceptions mean that we already found the
			 * correct alternative, but it could not be evaluated.
			 */
			parser.restoreState();
		}
		int aggregatedParsePosition = resultAggregator.getAggregatedParsePosition();
		parser.setParsePosition(aggregatedParsePosition);
		return resultAggregator.aggregate();
	}

	@Override
	public void parseSyntactically(Parser<S> parser) throws SyntaxException {
		if (alternatives.isEmpty()) {
			throw new SyntaxException("No alternatives have been defined for this or rule");
		}
		SyntaxException syntaxException = null;
		int maxParsePosition = -1;
		for (Rule<I, O, S> alternative : alternatives) {
			parser.storeState();
			SyntaxException exception = null;
			try {
				parser.parseSyntactically(alternative);
			} catch (SyntaxException e) {
				exception = e;
			}
			int parsePosition = parser.getParsePosition();
			if (parsePosition > maxParsePosition) {
				maxParsePosition = parsePosition;
				syntaxException = exception;
			}
			parser.restoreState();
		}
		parser.setParsePosition(maxParsePosition);
		if (syntaxException != null) {
			throw syntaxException;
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

	/**
	 * Aggregates the outcomes of parsing the different alternatives and eventually decides which alternative was
	 * (most likely) the correct one. The decision logic is currently as follows:
	 * <ul>
	 *     <li>
	 *         If there are {@link Parser.EventResultException}s (in most cases these will be code completions), then
	 *         these have the highest priority.
	 *     </li>
	 *     <li>
	 *         Otherwise, the outcome(s) with the highest parse position win(s). It is not clear that this heuristic
	 *         always yields the correct result, but it will work very often, and we hope that rules can be formulated
	 *         such that this heuristic works correctly.<br>
	 *         <br>
	 *         Note that an alternative that returns a result is not necessarily the correct one. The empty rule, which
	 *         is used, e.g., as alternative in method and constructor parameter lists, always succeeds, but this is not
	 *         always the correct rule to use.<br>
	 *         <br>
	 *         When multiple types of outcomes have the same parse position, ties are broken as follows:
	 *         <ol>
	 *             <li>Results have the highest priority.</li>
	 *             <li>{@link SemanticException}s have medium priority.</li>
	 *             <li>{@link SyntaxException}s have the lowest priority.</li>
	 *         </ol>
	 *     </li>
	 * </ul>
	 */
	private static class ResultAggregator<O>
	{
		private final List<Parser.EventResultException>		eventResultExceptions	= new ArrayList<>();
		private final List<O>								results					= new ArrayList<>();
		private final List<SemanticException>				semanticExceptions		= new ArrayList<>();
		private final List<SyntaxException>					syntaxExceptions		= new ArrayList<>();

		private int	maxParsePosition	= -1;

		public void addEventResultException(Parser.EventResultException eventResultException) {
			eventResultExceptions.add(eventResultException);
			results.clear();
			semanticExceptions.clear();
			syntaxExceptions.clear();
		}

		public void addResult(O result, int parsePosition) {
			if (!eventResultExceptions.isEmpty()) {
				// event result exceptions have the highest priority
				return;
			}
			if (parsePosition > maxParsePosition) {
				// prefer new result to previously found results
				results.clear();
			}
			if (parsePosition >= maxParsePosition) {
				maxParsePosition = parsePosition;
				results.add(result);
				// prefer result to semantic exceptions and syntax exceptions
				semanticExceptions.clear();
				syntaxExceptions.clear();
			}
		}

		public void addSemanticException(SemanticException semanticException, IntSupplier parsePositionSupplier) {
			if (!eventResultExceptions.isEmpty()) {
				// eventResultExceptions have the highest priority
				return;
			}
			int parsePosition = parsePositionSupplier.getAsInt();
			if (parsePosition > maxParsePosition) {
				// prefer semantic exception to previously found results and semantic exceptions
				results.clear();
				semanticExceptions.clear();
			}
			if (parsePosition >= maxParsePosition && results.isEmpty()) {
				maxParsePosition = parsePosition;
				semanticExceptions.add(semanticException);
				// prefer semantic exception to syntax exceptions
				syntaxExceptions.clear();
			}
		}

		public void addSyntaxException(SyntaxException syntaxException, int parsePosition) {
			if (!eventResultExceptions.isEmpty()) {
				// eventResultExceptions have the highest priority
				return;
			}
			if (parsePosition > maxParsePosition) {
				// prefer syntax exception to previously found results, semantic exceptions, and syntax exceptions
				results.clear();
				semanticExceptions.clear();
				syntaxExceptions.clear();
			}
			if (parsePosition >= maxParsePosition && results.isEmpty() && semanticExceptions.isEmpty()) {
				maxParsePosition = parsePosition;
				syntaxExceptions.add(syntaxException);
			}
		}

		public int getAggregatedParsePosition() {
			return maxParsePosition;
		}

		public O aggregate() throws Parser.EventResultException, SemanticException, SyntaxException {
			if (!eventResultExceptions.isEmpty()) {
				// TODO: Merge eventResultExceptions
				throw eventResultExceptions.get(0);
			} else if (!results.isEmpty()) {
				if (results.size() == 1) {
					return results.get(0);
				}
				// TODO: More details required
				throw new SemanticException("Ambiguous expression");
			} else if (!semanticExceptions.isEmpty()) {
				// TODO: Merge semantic exceptions
				throw semanticExceptions.get(0);
			} else if (!syntaxExceptions.isEmpty()) {
				// TODO: Merge syntax exceptions
				throw syntaxExceptions.get(0);
			} else {
				throw new IllegalStateException("The or rule seems to have no alternatives, but this case should have been handled before.");
			}
		}
	}
}

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
				resultAggregator.addSyntaxException(e);
			} catch (SemanticException e) {
				if (e.getSyntacticParsePosition() < 0) {
					parser.restoreState();
					parser.storeState();
					try {
						parser.parseSyntactically(alternative);
					} catch (SyntaxException ignored) {
						/* does not matter */
					}
					e.setSyntacticParsePosition(parser.getParsePosition());
				}
				resultAggregator.addSemanticException(e);
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
	 *         Otherwise, the outcome(s) with the highest syntactic parse position win(s). It is not clear that this
	 *         heuristic always yields the correct result, but it will work very often, and we hope that rules can be
	 *         formulated such that this heuristic works correctly.<br>
	 *         <br>
	 *         Note that an alternative that returns a result is not necessarily the correct one. The empty rule, which
	 *         is used, e.g., as alternative in method and constructor parameter lists, always succeeds, but this is not
	 *         always the correct rule to use.<br>
	 *         <br>
	 *         When multiple types of outcomes have the same parse position, then the one with the highest regular parse
	 *         position wins. Note that only for {@code SemanticException}s there is a difference between these two:
	 *         The "regular" parse position at which the {@code SemanticException} has occurred. The {@code OrRule}
	 *         will then try to parse the input further syntactically-only, potentially leading to a higher syntactic
	 *         parse position.
	 *         <br>
	 *         If multiple types of outcomes are equal with respect to the aforementioned metrics, then ties are broken
	 *         as follows:
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

		private ParseProgress	bestParseProgress	= new ParseProgress(-1);

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
			ParseProgress parseProgress = new ParseProgress(parsePosition);
			int comparison = parseProgress.compareTo(bestParseProgress);

			if (comparison > 0) {
				// prefer new result to previously found results
				results.clear();
			}
			if (comparison >= 0) {
				bestParseProgress = parseProgress;
				results.add(result);
				// prefer result to semantic exceptions and syntax exceptions
				semanticExceptions.clear();
				syntaxExceptions.clear();
			}
		}

		public void addSemanticException(SemanticException semanticException) {
			if (!eventResultExceptions.isEmpty()) {
				// eventResultExceptions have the highest priority
				return;
			}
			ParseProgress parseProgress = new ParseProgress(semanticException.getParsePosition(), semanticException.getSyntacticParsePosition());
			int comparison = parseProgress.compareTo(bestParseProgress);

			if (comparison > 0) {
				// prefer semantic exception to previously found results and semantic exceptions
				results.clear();
				semanticExceptions.clear();
			}
			if (comparison >= 0 && results.isEmpty()) {
				bestParseProgress = parseProgress;
				semanticExceptions.add(semanticException);
				// prefer semantic exception to syntax exceptions
				syntaxExceptions.clear();
			}
		}

		public void addSyntaxException(SyntaxException syntaxException) {
			if (!eventResultExceptions.isEmpty()) {
				// eventResultExceptions have the highest priority
				return;
			}
			ParseProgress parseProgress = new ParseProgress(syntaxException.getParsePosition());
			int comparison = parseProgress.compareTo(bestParseProgress);

			if (comparison > 0) {
				// prefer syntax exception to previously found results, semantic exceptions, and syntax exceptions
				results.clear();
				semanticExceptions.clear();
				syntaxExceptions.clear();
			}
			if (comparison >= 0 && results.isEmpty() && semanticExceptions.isEmpty()) {
				bestParseProgress = parseProgress;
				syntaxExceptions.add(syntaxException);
			}
		}

		public int getAggregatedParsePosition() {
			return bestParseProgress.getParsePosition();
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

		private static class ParseProgress implements Comparable<ParseProgress>
		{
			private final int	parsePosition;
			private final int	syntacticParsePosition;

			ParseProgress(int parsePosition) {
				this(parsePosition, parsePosition);
			}

			ParseProgress(int parsePosition, int syntacticParsePosition) {
				this.parsePosition = parsePosition;
				this.syntacticParsePosition = syntacticParsePosition;
			}

			int getParsePosition() {
				return parsePosition;
			}

			@Override
			public int compareTo(ParseProgress that) {
				if (syntacticParsePosition < that.syntacticParsePosition) {
					return -1;
				} else if (syntacticParsePosition > that.syntacticParsePosition) {
					return 1;
				}
				if (parsePosition < that.parsePosition) {
					return -1;
				} else if (parsePosition > that.parsePosition) {
					return 1;
				}
				return 0;
			}
		}
	}
}

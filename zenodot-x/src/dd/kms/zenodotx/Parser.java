package dd.kms.zenodotx;

import dd.kms.zenodotx.event.Event;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.rule.compound.CompoundRule;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Pattern;

public class Parser<S>
{
	private final CharacterStream	characterStream;
	private final int				eventPosition;
	private final Event				event;
	private final Deque<Integer>	characterStreamPositions	= new ArrayDeque<>();

	public Parser(String text, int eventPosition, Event event) {
		characterStream = new CharacterStream(text);
		this.eventPosition = eventPosition;
		this.event = event;
	}

	public <I, O> O parse(Rule<I, O, S> rule, I input, S settings) throws SyntaxException, SemanticException, EvaluationException, EventResultException {
		if (rule instanceof CompoundRule) {
			return ((CompoundRule<I, O, S>) rule).parse(input, settings, this);
		} else if (rule instanceof SimpleRule) {
			return parseSimpleRule((SimpleRule<I, O, S>) rule, input, settings);
		} else {
			throw new IllegalStateException("Cannot parse rule of type " + rule.getClass().getName()
				+ ". Only " + CompoundRule.class.getName() + " and " + SimpleRule.class.getName() + " are supported.");
		}
	}

	private <I, O> O parseSimpleRule(SimpleRule<I, O, S> rule, I input, S settings) throws SyntaxException, SemanticException, EvaluationException, EventResultException {
		int positionBeforeRegex = characterStream.getPosition();
		String parsedString = parseSimpleRuleSyntactically(rule);
		int positionAfterRegex = characterStream.getPosition();

		SemanticRule<I, O, S> semanticRule = rule.getSemanticRule();

		// for code completion and method overload proposal
		if (positionBeforeRegex <= eventPosition && eventPosition < positionAfterRegex) {
			// TODO: Configure which string the event (e.g. code completion) should be based on: The full parsed
			//       string or the parsed string until the event position
			semanticRule.handleEvent(event, input, parsedString, settings);
			throw new EventResultException();
		}

		return semanticRule.evaluate(input, parsedString, settings);
	}

	public void parseSyntactically(Rule<?, ?, S> rule) throws SyntaxException {
		if (rule instanceof CompoundRule) {
			((CompoundRule<?, ?, S>) rule).parseSyntactically(this);
		} else if (rule instanceof SimpleRule) {
			parseSimpleRuleSyntactically((SimpleRule<?, ?, S>) rule);
		} else {
			throw new IllegalStateException("Cannot parse rule of type " + rule.getClass().getName()
				+ ". Only " + CompoundRule.class.getName() + " and " + SimpleRule.class.getName() + " are supported.");
		}
	}

	private String parseSimpleRuleSyntactically(SimpleRule<?, ?, S> rule) throws SyntaxException {
		SyntaxRule syntaxRule = rule.getSyntaxRule();
		// TODO: Whitespace handling
		// TODO: Not clear whether every case can be handled by a regex. What if code completion is
		//       requested, but the regex does not match the current string? What to do the code completion
		//       on then?
		Pattern regex = syntaxRule.getRegex();
		// TODO: What should the message be?
		return characterStream.readRegex(regex).orElseThrow(SyntaxException::new);
	}

	public void storeState() {
		characterStreamPositions.push(characterStream.getPosition());
	}

	public void restoreState() {
		characterStream.setPosition(characterStreamPositions.pop());
	}

	public void dropStoredState() {
		characterStreamPositions.pop();
	}

	public int getParsePosition() {
		return characterStream.getPosition();
	}

	public void setParsePosition(int parsePosition) {
		characterStream.setPosition(parsePosition);
	}

	/**
	 * Internal exception to indicate that an event (e.g. code completion) has handled. This exception is required
	 * to give callers a chance to react accordingly: Sequence parsing will stop, while Or parsing will try another
	 * branch to collect event "reactions" (e.g. code completions) from different branches.
	 *
	 * TODO: Make top-level class
 	 */
	public static class EventResultException extends Exception {}
}

package dd.kms.zenodotx;

import dd.kms.zenodotx.event.Event;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.rule.compound.CompoundRule;
import dd.kms.zenodotx.rule.compound.OrRule;
import dd.kms.zenodotx.rule.compound.RepetitionRule;
import dd.kms.zenodotx.rule.compound.SequenceRule;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;
import dd.kms.zenodotx.state.State;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Parser<S extends State>
{
	private final CharacterStream	characterStream;
	private final int				eventPosition;
	private final Event				event;
	private S						state;

	public Parser(String text, int eventPosition, Event event, S initialState) {
		characterStream = new CharacterStream(text);
		this.eventPosition = eventPosition;
		this.event = event;
		state = initialState;
	}

	public void parse(CompoundRule<S> rule) throws SyntaxException, SemanticException, EvaluationException {
		try {
			parseCompoundRule(rule);
		} catch (HandledEventException e) {
			/*
			 * Ok: reactions have been collected. This exception is only required to handle the control flow within
			 * the parser. It is not meant to be propagated to the outside.
			 */
		}
	}

	private void parseCompoundRule(CompoundRule<S> rule) throws SyntaxException, SemanticException, EvaluationException, HandledEventException {
		if (rule instanceof SequenceRule) {
			parseSequence((SequenceRule<S>) rule);
		} else if (rule instanceof OrRule) {
			parseOr((OrRule<S>) rule);
		} else if (rule instanceof RepetitionRule) {
			parseRepetition((RepetitionRule<S>) rule);
		} else {
			throw new IllegalStateException("Cannot parse element rule of type " + rule.getClass().getName()
				+ ". Only " + SequenceRule.class.getName() + " and "
				+ OrRule.class.getName() + " and "
				+ RepetitionRule.class.getName() + " are supported.");
		}
	}

	private void parseSequence(SequenceRule<S> rule) throws SyntaxException, SemanticException, EvaluationException, HandledEventException {
		List<Rule<S>> sequence = rule.getSequence();
		int origPosition = characterStream.getPosition();
		S origState = (S) state.copy();
		try {
			for (Rule<S> element : sequence) {
				parseRule(element);
			}
		} catch (SyntaxException | SemanticException e) {
			characterStream.setPosition(origPosition);
			state = origState;
			throw e;
		}
		/*
		 * HandledEventException and EvaluationException are propagated directly because it was correct
		 * to parse this rule again, but now we cannot proceed.
		 */
	}

	private void parseOr(OrRule<S> rule) throws SyntaxException, SemanticException, EvaluationException, HandledEventException {
		List<Rule<S>> alternatives = rule.getAlternatives();
		List<SyntaxException> syntaxExceptions = new ArrayList<>();
		List<SemanticException> semanticExceptions = new ArrayList<>();
		boolean handledEvent = false;
		for (Rule<S> alternative : alternatives) {
			int origPosition = characterStream.getPosition();
			S origState = (S) state.copy();
			try {
				parseRule(alternative);
				return;
			} catch (SyntaxException e) {
				// continue with next alternative
				syntaxExceptions.add(e);
			} catch (SemanticException e) {
				// continue with next alternative
				semanticExceptions.add(e);
			} catch (HandledEventException e) {
				// continue with next alternative
				handledEvent = true;
			}
			/*
			 * An EvaluationException is directly propagated to the caller because it means that we have found
			 * the correct alternative.
			 */
			characterStream.setPosition(origPosition);
			state = origState;
		}
		if (handledEvent) {
			throw new HandledEventException();
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

	private void parseRepetition(RepetitionRule<S> rule) throws SyntaxException, SemanticException, EvaluationException, HandledEventException {
		Rule<S> ruleToRepeat = rule.getRuleToRepeat();
		while (true) {
			int origPosition = characterStream.getPosition();
			S origState = (S) state.copy();
			try {
				parseRule(ruleToRepeat);
			} catch (SyntaxException | SemanticException e) {
				characterStream.setPosition(origPosition);
				state = origState;
				return;
			}
			/*
			 * HandledEventException and EvaluationException are propagated directly because it was correct
			 * to parse this rule again, but now we cannot proceed.
			 */
		}
	}

	private void parseRule(Rule<S> rule) throws SyntaxException, SemanticException, EvaluationException, HandledEventException {
		if (rule instanceof CompoundRule) {
			parseCompoundRule((CompoundRule<S>) rule);
		} else if (rule instanceof SimpleRule) {
			parseSimpleRule((SimpleRule<S>) rule);
		} else {
			throw new IllegalStateException("Cannot parse rule of type " + rule.getClass().getClass().getName()
				+ ". Only " + CompoundRule.class.getName() + " and " + SimpleRule.class.getName() + " are supported.");
		}
	}

	private void parseSimpleRule(SimpleRule<S> rule) throws SyntaxException, SemanticException, EvaluationException, HandledEventException {
		SyntaxRule syntaxRule = rule.getSyntaxRule();
		// TODO: Whitespace handling
		// TODO: Not clear whether every case can be handled by a regex. What if code completion is
		//       requested, but the regex does not match the current string? What to do the code completion
		//       on then?
		int positionBeforeRegex = characterStream.getPosition();
		Pattern regex = syntaxRule.getRegex();
		// TODO: What should the message be?
		String parsedString = characterStream.readRegex(regex).orElseThrow(SyntaxException::new);
		int positionAfterRegex = characterStream.getPosition();

		SemanticRule<S> semanticRule = rule.getSemanticRule();

		// for code completion and method overload proposal
		if (positionBeforeRegex <= eventPosition && eventPosition < positionAfterRegex) {
			// TODO: Configure which string the event (e.g. code completion) should be based on: The full parsed
			//       string or the parsed string until the event position
			semanticRule.handleEvent(event, parsedString, state);
			throw new HandledEventException();
		}

		semanticRule.evaluate(parsedString, state);
	}

	/**
	 * Internal exception to indicate that an event (e.g. code completion) has handled. This exception is required
	 * to give callers a chance to react accordingly: Sequence parsing will stop, while Or parsing will try another
	 * branch to collect event "reactions" (e.g. code completions) from different branches.
 	 */
	private static class HandledEventException extends Exception {}
}

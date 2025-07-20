package dd.kms.zenodotx;

import dd.kms.zenodotx.event.Event;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.rule.EvaluationRule;
import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.rule.compound.CompoundRule;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;
import dd.kms.zenodotx.stack.Stack;

import java.util.Optional;
import java.util.regex.Pattern;

public class Parser<S extends GrammarSettings>
{
	private final String				text;
	private final CharacterStream		characterStream;
	private final int					eventPosition;
	private final Event					event;
	private Stack<RuleInfo>				parsedRules		= new Stack<>();

	public Parser(String text, int eventPosition, Event event) {
		this.text = text;
		characterStream = new CharacterStream(text);
		this.eventPosition = eventPosition;
		this.event = event;
	}

	public <I, O> O parse(Rule<I, O, S> rule, I input, S settings) throws SyntaxException, SemanticException, EvaluationException, EventResultException {
		try {
			if (rule instanceof CompoundRule) {
				return ((CompoundRule<I, O, S>) rule).parse(input, settings, this);
			} else if (rule instanceof SimpleRule) {
				return parseSimpleRule((SimpleRule<I, O, S>) rule, input, settings);
			} else if (rule instanceof EvaluationRule) {
				return ((EvaluationRule<I, O, S>) rule).evaluate(input, settings);
			} else {
				throw new IllegalStateException("Cannot parse rule of type " + rule.getClass().getName()
					+ ". Only " + CompoundRule.class.getName() + " and " + SimpleRule.class.getName() + " are supported.");
			}
		} catch (SyntaxException e) {
			if (e.getParsePosition() < 0) {
				e.setParsePosition(getParsePosition());
			}
			throw e;
		} catch (SemanticException e) {
			if (e.getParsePosition() < 0) {
				e.setParsePosition(getParsePosition());
			}
			throw e;
		}
	}

	private <I, O> O parseSimpleRule(SimpleRule<I, O, S> rule, I input, S settings) throws SyntaxException, SemanticException, EvaluationException, EventResultException {
		int positionBeforeRegex = characterStream.getPosition();
		String parsedString = parseSimpleRuleSyntactically(rule, settings);
		int positionAfterRegex = characterStream.getPosition();

		SemanticRule<I, O, S> semanticRule = rule.getSemanticRule();

		// for code completion and method overload proposal
		if (positionBeforeRegex <= eventPosition && eventPosition < positionAfterRegex) {
			// TODO: Configure which string the event (e.g. code completion) should be based on: The full parsed
			//       string or the parsed string until the event position
			semanticRule.handleEvent(event, input, parsedString, settings);
			throw new EventResultException();
		}

		O result = semanticRule.evaluate(input, parsedString, settings);
		RuleInfo ruleInfo = new RuleInfo(positionBeforeRegex, positionAfterRegex, text, rule);
		parsedRules.push(ruleInfo);
		return result;
	}

	public void parseSyntactically(Rule<?, ?, S> rule, S settings) throws SyntaxException {
		try {
			if (rule instanceof CompoundRule) {
				((CompoundRule<?, ?, S>) rule).parseSyntactically(this, settings);
			} else if (rule instanceof SimpleRule) {
				parseSimpleRuleSyntactically((SimpleRule<?, ?, S>) rule, settings);
			} else if (rule instanceof EvaluationRule) {
				// evaluation rules don't have a syntax
			} else {
				throw new IllegalStateException("Cannot parse rule of type " + rule.getClass().getName()
					+ ". Only " + CompoundRule.class.getName() + " and " + SimpleRule.class.getName() + " are supported.");
			}
		} catch (SyntaxException e) {
			if (e.getParsePosition() < 0) {
				e.setParsePosition(getParsePosition());
			}
			throw e;
		}
	}

	private String parseSimpleRuleSyntactically(SimpleRule<?, ?, S> rule, S settings) throws SyntaxException {
		SyntaxRule syntaxRule = rule.getSyntaxRule();
		// TODO: Not clear whether every case can be handled by a regex. What if code completion is
		//       requested, but the regex does not match the current string? What to do the code completion
		//       on then?
		Pattern regex = syntaxRule.getRegex();

		Pattern charactersToIgnorePattern = settings.getCharactersToIgnorePattern();
		ParserState initialState = getParserState();

		Optional<String> skippedCharacters = characterStream.readRegex(charactersToIgnorePattern);

		Optional<String> parsedString = characterStream.readRegex(regex);
		if (parsedString.isPresent()) {
			return parsedString.get();
		}
		if (skippedCharacters.isPresent() && !skippedCharacters.get().isEmpty()) {
			/*
			 * We had skipped the characters to ignore, but in some cases this might be wrong.
			 * Example: In Java, whitespaces can usually be ignored. This is not the case for, e.g.,
			 * "new XYZ()", where "XYZ" is a class name. Here, the space between "new" and "XYZ()" is
			 * relevant. The whitespace must not be skipped in this case.
			 */
			setParserState(initialState);

			parsedString = characterStream.readRegex(regex);
			if (parsedString.isPresent()) {
				return parsedString.get();
			}
		}
		setParserState(initialState);
		throw new SyntaxException("Unexpected characters. Expected: " + syntaxRule.getSyntaxDescription(), initialState.getParsePosition());
	}

	public ParserState getParserState() {
		return new ParserState(getParsePosition(), parsedRules);
	}

	public void setParserState(ParserState parserState) {
		setParsePosition(parserState.getParsePosition());
		parsedRules = parserState.getParsedRules();
	}

	public int getParsePosition() {
		return characterStream.getPosition();
	}

	private void setParsePosition(int parsePosition) {
		characterStream.setPosition(parsePosition);
	}

	@Override
	public String toString() {
		return characterStream.toString();
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

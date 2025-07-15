package dd.kms.zenodotx;

import dd.kms.zenodotx.stack.Stack;

public class ParserState
{
	private final int				parsePosition;
	private final Stack<RuleInfo>	parsedRules;

	public ParserState(int parsePosition, Stack<RuleInfo> parsedRules) {
		this.parsePosition = parsePosition;
		this.parsedRules = parsedRules.copy();
	}

	public int getParsePosition() {
		return parsePosition;
	}

	public Stack<RuleInfo> getParsedRules() {
		return parsedRules.copy();
	}
}

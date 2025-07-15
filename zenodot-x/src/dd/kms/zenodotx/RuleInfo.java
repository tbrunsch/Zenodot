package dd.kms.zenodotx;

import dd.kms.zenodotx.rule.simple.SimpleRule;

public class RuleInfo
{
	private final int					startPosition;
	private final int					endPosition;
	private final String				text;
	private final SimpleRule<?, ?, ?>	rule;

	public RuleInfo(int startPosition, int endPosition, String text, SimpleRule<?, ?, ?> rule) {
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.text = text;
		this.rule = rule;
	}

	public int getStartPosition() {
		return startPosition;
	}

	public int getEndPosition() {
		return endPosition;
	}

	public SimpleRule<?, ?, ?> getRule() {
		return rule;
	}

	@Override
	public String toString() {
		return text.substring(startPosition, endPosition) + "  =  " + rule;
	}
}

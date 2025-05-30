package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.rule.simple.SyntaxRule;

import java.util.regex.Pattern;

public class IdentifierSyntaxRule implements SyntaxRule
{
	private static final Pattern	IDENTIFIER_PATTERN	= Pattern.compile("[_\\$A-Za-z][_\\$A-Za-z0-9]*");

	public static final SyntaxRule	RULE	= new IdentifierSyntaxRule();

	@Override
	public Pattern getRegex() {
		return IDENTIFIER_PATTERN;
	}
}

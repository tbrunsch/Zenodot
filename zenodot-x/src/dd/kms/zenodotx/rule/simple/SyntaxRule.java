package dd.kms.zenodotx.rule.simple;

import java.util.regex.Pattern;

public interface SyntaxRule
{
	Pattern getRegex();
	String getSyntaxDescription();
}

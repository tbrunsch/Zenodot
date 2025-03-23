package dd.kms.zenodotx.java.test;

import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodotx.Parser;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.java.JavaState;
import dd.kms.zenodotx.java.ParserSettings;
import dd.kms.zenodotx.java.rule.JavaRuleSet;
import dd.kms.zenodotx.rule.PreparsedGrammar;
import dd.kms.zenodotx.rule.Rule;

public class FieldTest
{
	public static void main(String[] args) throws SyntaxException, EvaluationException, SemanticException {
		TestClass testInstance = new TestClass();

		String expression = "o.o.l";

		JavaState state = new JavaState(testInstance);
		state.setParserSettings(new ParserSettings(EvaluationMode.MIXED, AccessModifier.PRIVATE));

		JavaRuleSet javaRuleSet = new JavaRuleSet();
		Rule<JavaState> fullExpression = javaRuleSet.getFullExpression();
		Parser<JavaState> parser = new Parser<>(expression, -1, null, state);
		parser.parse(fullExpression);

		PreparsedGrammar<JavaState> preparsedGrammar = state.getPreparsedGrammar();
		state = new JavaState(testInstance);
		state.setParserSettings(new ParserSettings(EvaluationMode.MIXED, AccessModifier.PRIVATE));
		preparsedGrammar.evaluate(state);

		Object o = state.getEvaluatedObject();
		System.out.println(o);
	}

	private static class TestClass
	{
		private final Object	o = this;
		private final int i = 3;
		private final double d = 2.4;
		private final String s = "xyz";
		private final Object l = 1L;
	}
}

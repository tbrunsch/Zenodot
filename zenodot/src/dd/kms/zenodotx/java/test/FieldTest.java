package dd.kms.zenodotx.java.test;

import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.Parser;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.java.rule.JavaRuleSet;
import dd.kms.zenodotx.rule.Rule;

public class FieldTest
{
	public static void main(String[] args) throws SyntaxException, EvaluationException, SemanticException, Parser.EventResultException {
		TestClass testInstance = new TestClass();

		String expression = "o.o.l";

		JavaSettings settings = new JavaSettings(InfoProvider.createObjectInfo(testInstance), EvaluationMode.MIXED, AccessModifier.PRIVATE, AccessModifier.PROTECTED);

		JavaRuleSet javaRuleSet = new JavaRuleSet();
		Rule<Void, InstanceParseResult, JavaSettings> fullExpression = javaRuleSet.getFullExpression();
		Parser<JavaSettings> parser = new Parser<>(expression, -1, null);
		InstanceParseResult result = parser.parse(fullExpression, null, settings);
		ObjectInfo resultInfo = result.getEvaluatedResult();
		System.out.println(resultInfo.getObject());
		System.out.println(resultInfo.getDeclaredType());
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

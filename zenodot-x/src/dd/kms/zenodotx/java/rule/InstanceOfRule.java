package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.common.ObjectInfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodot.impl.utils.dataproviders.OperatorResultProvider;
import dd.kms.zenodotx.Parser;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.rule.compound.CompoundRule;

public class InstanceOfRule extends AbstractRule<InstanceParseResult, InstanceParseResult, JavaSettings> implements CompoundRule<InstanceParseResult, InstanceParseResult, JavaSettings>
{
	private final Rule<Void, Class<?>, JavaSettings>	classRule;

	public InstanceOfRule(Rule<Void, Class<?>, JavaSettings> classRule) {
		this.classRule = classRule;
	}

	@Override
	public InstanceParseResult parse(InstanceParseResult instance, JavaSettings settings, Parser<JavaSettings> parser) throws SyntaxException, EvaluationException, SemanticException, Parser.EventResultException {
		Class<?> clazz = parser.parse(classRule, null, settings);
		return new InstanceOfParseResult(instance, clazz, settings.getEvaluationMode());
	}

	private static class InstanceOfParseResult implements InstanceParseResult
	{
		private final InstanceParseResult	instance;
		private final Class<?>				clazz;
		private final ObjectInfo			evaluatedResult;

		private InstanceOfParseResult(InstanceParseResult instance, Class<?> clazz, EvaluationMode evaluationMode) throws EvaluationException {
			this.instance = instance;
			this.clazz = clazz;
			this.evaluatedResult = evaluate(instance.getEvaluatedResult(), evaluationMode);
		}

		@Override
		public ObjectInfo getEvaluatedResult() {
			return evaluatedResult;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, Variables variables, EvaluationMode evaluationMode) throws EvaluationException {
			return evaluate(instance.evaluate(thisInfo, variables, evaluationMode), evaluationMode);
		}

		private ObjectInfo evaluate(ObjectInfo instanceInfo, EvaluationMode evaluationMode) throws EvaluationException {
			ObjectInfoProvider objectInfoProvider = new ObjectInfoProvider(evaluationMode);
			OperatorResultProvider operatorResultProvider = new OperatorResultProvider(objectInfoProvider, evaluationMode);
			try {
				return operatorResultProvider.getInstanceOfInfo(instanceInfo, clazz);
			} catch (OperatorResultProvider.OperatorException e) {
				throw new EvaluationException(e.getMessage());
			}
		}
	}
}

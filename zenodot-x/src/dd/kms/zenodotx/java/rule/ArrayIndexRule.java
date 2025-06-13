package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.api.common.ReflectionUtils;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.common.ObjectInfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.Parser;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.rule.compound.CompoundRule;

public class ArrayIndexRule extends AbstractRule<InstanceParseResult, ArrayAccessInfo, JavaSettings> implements CompoundRule<InstanceParseResult, ArrayAccessInfo, JavaSettings>
{
	private final Rule<Void, InstanceParseResult, JavaSettings> expressionRule;

	public ArrayIndexRule(Rule<Void, InstanceParseResult, JavaSettings> expressionRule) {
		this.expressionRule = expressionRule;
	}

	@Override
	public ArrayAccessInfo parse(InstanceParseResult array, JavaSettings settings, Parser<JavaSettings> parser) throws SyntaxException, EvaluationException, SemanticException, Parser.EventResultException {
		EvaluationMode evaluationMode = settings.getEvaluationMode();
		ObjectInfoProvider objectInfoProvider = new ObjectInfoProvider(evaluationMode);

		ObjectInfo arrayInfo = array.getEvaluatedResult();
		Class<?> arrayType = objectInfoProvider.getType(arrayInfo);
		if (!arrayType.isArray()) {
			throw new SemanticException("Class '" + arrayType.getName() + "' is no array");
		}

		InstanceParseResult index = parser.parse(expressionRule, null, settings);
		ObjectInfo indexInfo = index.getEvaluatedResult();
		Class<?> indexType = objectInfoProvider.getType(indexInfo);
		boolean validIndexType = ReflectionUtils.isPrimitiveConvertibleTo(indexType, int.class, false);
		if (!validIndexType) {
			throw new SemanticException("Index must be of type 'int', but is of type " + indexType.getName());
		}
		return new ArrayAccessInfo(array, index);
	}

	@Override
	protected String getGenericName() {
		return "...[index]";
	}
}

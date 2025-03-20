package dd.kms.zenodotx.java;

import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.settings.EvaluationMode;

public class ParserSettings
{
	private final EvaluationMode	evaluationMode;
	private final AccessModifier	minimumFieldAccessModifier;

	public ParserSettings(EvaluationMode evaluationMode, AccessModifier minimumFieldAccessModifier) {
		this.evaluationMode = evaluationMode;
		this.minimumFieldAccessModifier = minimumFieldAccessModifier;
	}

	public EvaluationMode getEvaluationMode() {
		return evaluationMode;
	}

	public AccessModifier getMinimumFieldAccessModifier() {
		return minimumFieldAccessModifier;
	}
}

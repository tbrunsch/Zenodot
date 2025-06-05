package dd.kms.zenodotx.java;

import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;

public class JavaSettings
{
	private final ObjectInfo		thisInfo;
	private final EvaluationMode	evaluationMode;
	private final AccessModifier	minimumFieldAccessModifier;
	private final AccessModifier	minimumMethodAccessModifier;

	public JavaSettings(ObjectInfo thisInfo, EvaluationMode evaluationMode, AccessModifier minimumFieldAccessModifier, AccessModifier minimumMethodAccessModifier) {
		this.thisInfo = thisInfo;
		this.evaluationMode = evaluationMode;
		this.minimumFieldAccessModifier = minimumFieldAccessModifier;
		this.minimumMethodAccessModifier = minimumMethodAccessModifier;
	}

	public ObjectInfo getThisInfo() {
		return thisInfo;
	}

	public EvaluationMode getEvaluationMode() {
		return evaluationMode;
	}

	public AccessModifier getMinimumFieldAccessModifier() {
		return minimumFieldAccessModifier;
	}

	public AccessModifier getMinimumMethodAccessModifier() {
		return minimumMethodAccessModifier;
	}

	public JavaSettings withoutEvaluation() {
		return forEvaluationMode(EvaluationMode.STATIC_TYPING);
	}

	private JavaSettings forEvaluationMode(EvaluationMode newEvaluationMode) {
		return newEvaluationMode == evaluationMode
			? this
			: new JavaSettings(thisInfo, newEvaluationMode, minimumFieldAccessModifier, minimumMethodAccessModifier);
	}
}

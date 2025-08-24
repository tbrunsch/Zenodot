package dd.kms.zenodotx.java;

import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;

public class JavaSettingsBuilder
{
	private ObjectInfo			thisInfo					= InfoProvider.NULL_LITERAL;
	private EvaluationMode		evaluationMode				= EvaluationMode.STATIC_TYPING;
	private AccessModifier		minimumFieldAccessModifier	= AccessModifier.PRIVATE;
	private AccessModifier		minimumMethodAccessModifier	= AccessModifier.PRIVATE;

	public JavaSettingsBuilder thisInfo(ObjectInfo thisInfo) {
		this.thisInfo = thisInfo;
		return this;
	}

	public JavaSettingsBuilder evaluationMode(EvaluationMode evaluationMode) {
		this.evaluationMode = evaluationMode;
		return this;
	}

	public JavaSettingsBuilder minimumFieldAccessModifier(AccessModifier minimumFieldAccessModifier) {
		this.minimumFieldAccessModifier = minimumFieldAccessModifier;
		return this;
	}

	public JavaSettingsBuilder minimumMethodAccessModifier(AccessModifier minimumMethodAccessModifier) {
		this.minimumMethodAccessModifier = minimumMethodAccessModifier;
		return this;
	}

	public JavaSettings build() {
		return new JavaSettings(thisInfo, evaluationMode, minimumFieldAccessModifier, minimumMethodAccessModifier);
	}
}

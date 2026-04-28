package dd.kms.zenodotx.java;

import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.GrammarSettings;

import java.util.regex.Pattern;

public class JavaSettings implements GrammarSettings
{
	private static final Pattern	CHARACTERS_TO_IGNORE_PATTERN	= Pattern.compile("\\s*");

	private final ObjectInfo		thisInfo;
	private final EvaluationMode	evaluationMode;
	private final AccessModifier	minimumFieldAccessModifier;
	private final AccessModifier	minimumMethodAccessModifier;
	private final Variables			variables;

	public JavaSettings(ObjectInfo thisInfo, EvaluationMode evaluationMode, AccessModifier minimumFieldAccessModifier, AccessModifier minimumMethodAccessModifier, Variables variables) {
		this.thisInfo = thisInfo;
		this.evaluationMode = evaluationMode;
		this.minimumFieldAccessModifier = minimumFieldAccessModifier;
		this.minimumMethodAccessModifier = minimumMethodAccessModifier;
		this.variables = variables;
	}

	@Override
	public Pattern getCharactersToIgnorePattern() {
		return CHARACTERS_TO_IGNORE_PATTERN;
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

	public Variables getVariables() {
		return variables;
	}

	private JavaSettings forEvaluationMode(EvaluationMode newEvaluationMode) {
		return newEvaluationMode == evaluationMode
			? this
			: new JavaSettings(thisInfo, newEvaluationMode, minimumFieldAccessModifier, minimumMethodAccessModifier, variables);
	}
}

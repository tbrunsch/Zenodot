package dd.kms.zenodot.impl.settings;

import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodot.api.settings.*;
import dd.kms.zenodot.api.wrappers.PackageInfo;

import java.util.List;
import java.util.Set;

class ParserSettingsImpl implements ParserSettings
{
	private final CompletionMode	completionMode;
	private final Imports			imports;
	private final VariablePool 		variablePool;
	private final AccessModifier	minimumAccessModifier;
	private final EvaluationMode	evaluationMode;
	private final boolean 			considerAllClassesForClassCompletions;
	private final ObjectTreeNode	customHierarchyRoot;
	private final ParserLogger 		logger;

	ParserSettingsImpl(CompletionMode completionMode, Set<Class<?>> importedClasses, Set<PackageInfo> importedPackages, List<Variable> variables, AccessModifier minimumAccessModifier, EvaluationMode evaluationMode, boolean considerAllClassesForClassCompletions, ObjectTreeNode customHierarchyRoot, ParserLogger logger) {
		this.completionMode = completionMode;
		this.imports = new ImportsImpl(importedClasses, importedPackages);
		this.variablePool = new VariablePool(variables);
		this.minimumAccessModifier = minimumAccessModifier;
		this.evaluationMode = evaluationMode;
		this.considerAllClassesForClassCompletions = considerAllClassesForClassCompletions;
		this.customHierarchyRoot = customHierarchyRoot;
		this.logger = logger;
	}

	@Override
	public CompletionMode getCompletionMode() {
		return completionMode;
	}

	@Override
	public Imports getImports() {
		return imports;
	}

	@Override
	public List<Variable> getVariables() {
		return variablePool.getVariables();
	}

	@Override
	public AccessModifier getMinimumAccessModifier() {
		return minimumAccessModifier;
	}

	@Override
	public EvaluationMode getEvaluationMode() {
		return evaluationMode;
	}

	@Override
	public boolean isConsiderAllClassesForClassCompletions() {
		return considerAllClassesForClassCompletions;
	}

	@Override
	public ObjectTreeNode getCustomHierarchyRoot() {
		return customHierarchyRoot;
	}

	@Override
	public ParserLogger getLogger() {
		return logger;
	}

	@Override
	public ParserSettingsBuilder builder() {
		return new ParserSettingsBuilderImpl(this);
	}
}

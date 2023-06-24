package dd.kms.zenodot.impl.settings;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodot.api.settings.*;

import java.util.List;
import java.util.Set;

class ParserSettingsImpl implements ParserSettings
{
	private final CompletionMode	completionMode;
	private final List<String>		innerClassNames;
	private final Imports			imports;
	private final AccessModifier	minimumAccessModifier;
	private final EvaluationMode	evaluationMode;
	private final boolean 			considerAllClassesForClassCompletions;
	private final ObjectTreeNode	customHierarchyRoot;
	private final ParserLogger 		logger;

	ParserSettingsImpl(CompletionMode completionMode, List<String> innerClassNames, Set<Class<?>> importedClasses, Set<String> importedPackages, AccessModifier minimumAccessModifier, EvaluationMode evaluationMode, boolean considerAllClassesForClassCompletions, ObjectTreeNode customHierarchyRoot, ParserLogger logger) {
		this.completionMode = completionMode;
		this.innerClassNames = ImmutableList.copyOf(innerClassNames);
		this.imports = new ImportsImpl(importedClasses, importedPackages);
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
	public List<String> getInnerClassNames() {
		return innerClassNames;
	}

	@Override
	public Imports getImports() {
		return imports;
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

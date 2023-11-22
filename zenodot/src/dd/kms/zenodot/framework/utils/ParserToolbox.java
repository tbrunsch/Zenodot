package dd.kms.zenodot.framework.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.CompletionMode;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.parsers.CompletionProvider;
import dd.kms.zenodot.framework.flowcontrol.InternalErrorException;
import dd.kms.zenodot.framework.operators.BinaryOperator;
import dd.kms.zenodot.framework.parsers.AbstractParser;
import dd.kms.zenodot.framework.parsers.CallerContext;
import dd.kms.zenodot.framework.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.framework.parsers.expectations.ParseResultExpectation;
import dd.kms.zenodot.framework.result.CodeCompletions;
import dd.kms.zenodot.framework.result.ObjectParseResult;
import dd.kms.zenodot.framework.result.ParseResult;
import dd.kms.zenodot.framework.tokenizer.CompletionGenerator;
import dd.kms.zenodot.framework.tokenizer.TokenStream;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class used by most parsers to access other parsers, data providers,
 * and the parser settings.
 */
public class ParserToolbox
{
	private final ObjectInfo		thisInfo;
	private final ParserSettings	settings;
	private final Variables			variables;

	private final Map<Class<?>, Object>	instanceRegistry	= new HashMap<>();

	public ParserToolbox(ObjectInfo thisInfo, ParserSettings settings, Variables variables) {
		this.thisInfo = thisInfo;
		this.settings = settings;
		this.variables = variables;

		EvaluationMode evaluationMode = settings.getEvaluationMode();
		ParserLogger logger = settings.getLogger();

		registerInstance(this);
		registerInstance(variables);
		registerInstance(evaluationMode);
		registerInstance(logger);
	}

	public ObjectInfo getThisInfo() {
		return thisInfo;
	}

	public ParserSettings getSettings() {
		return settings;
	}

	public Variables getVariables() {
		return variables;
	}

	public ParserToolbox withEvaluationMode(EvaluationMode newEvaluationMode) {
		if (settings.getEvaluationMode() == newEvaluationMode) {
			return this;
		}
		ParserSettings newSettings = settings.builder()
			.evaluationMode(newEvaluationMode)
			.build();
		return new ParserToolbox(thisInfo, newSettings, variables);
	}

	public ParserToolbox withVariables(Variables newVariables) {
		return new ParserToolbox(thisInfo, settings, newVariables);
	}

	public <T> T inject(Class<T> clazz) {
		Object instance = injectRecursively(clazz, new HashSet<>());
		return clazz.cast(instance);
	}

	private Object injectRecursively(Class<?> clazz, Set<Class<?>> pendingClasses) {
		Preconditions.checkArgument(!clazz.isPrimitive(), "Cannot inject primitive class " + clazz);
		Preconditions.checkArgument(!pendingClasses.contains(clazz), "Detected cyclic dependencies during injection");
		pendingClasses.add(clazz);

		Object instance = instanceRegistry.get(clazz);
		if (instance != null) {
			return instance;
		}

		List<Constructor<?>> constructors = Arrays.stream(clazz.getConstructors())
			.filter(c -> Modifier.isPublic(c.getModifiers()))
			.collect(Collectors.toList());
		Preconditions.checkArgument(constructors.size() == 1, "Cannot inject class " + clazz + " because it must have exactly one public constructor");
		Constructor<?> constructor = Iterables.getOnlyElement(constructors);
		Class<?>[] parameterTypes = constructor.getParameterTypes();
		int numParameters = parameterTypes.length;
		Object[] parameters = new Object[numParameters];
		for (int i = 0; i < numParameters; i++) {
			Class<?> parameterType = parameterTypes[i];
			List<Object> registeredInstances = getRegisteredInstances(parameterType);
			Preconditions.checkArgument(registeredInstances.size() <= 1, "Cannot inject class " + clazz + " because multiple instances are registered for constructor argument " + (i+1) + " of type " + parameterType);
			parameters[i] = registeredInstances.isEmpty()
					? injectRecursively(parameterType, pendingClasses)
					: Iterables.getOnlyElement(registeredInstances);
		}
		try {
			instance = constructor.newInstance(parameters);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new IllegalArgumentException("Cannot instantiate class " + clazz + ": " + e, e);
		}
		registerInstance(instance);
		return instance;
	}

	private void registerInstance(Object instance) {
		instanceRegistry.put(instance.getClass(), instance);
	}

	private List<Object> getRegisteredInstances(Class<?> clazz) {
		List<Object> registeredInstances = new ArrayList<>();
		for (Map.Entry<Class<?>, Object> entry : instanceRegistry.entrySet()) {
			if (clazz.isAssignableFrom(entry.getKey())) {
				registeredInstances.add(entry.getValue());
			}
		}
		return registeredInstances;
	}

	public CompletionGenerator getStringLiteralCompletionGenerator(@Nullable CallerContext callerContext) {
		if (callerContext == null) {
			return TokenStream.NO_COMPLETIONS;
		}
		Set<Executable> executables = callerContext.getExecutables();
		int paramIndex = callerContext.getPreviousParameters().size();
		List<CompletionProvider> completionProviders = new ArrayList<>();
		for (Executable executable : executables) {
			List<CompletionProvider> completionProvidersForExecutable = settings.getStringLiteralCompletionProviders(executable, paramIndex);
			completionProviders.addAll(completionProvidersForExecutable);
		}
		if (completionProviders.isEmpty()) {
			return TokenStream.NO_COMPLETIONS;
		}
		ParserSettings settings = getSettings();
		CompletionMode completionMode = settings.getCompletionMode();
		return completionInfo -> {
			List<CodeCompletion> completions = new ArrayList<>();
			for (CompletionProvider completionProvider : completionProviders) {
				List<? extends CodeCompletion> completionsOfProvider = completionProvider.getCodeCompletions(completionInfo, completionMode, callerContext);
				completions.addAll(completionsOfProvider);
			}
			return new CodeCompletions(completions);
		};
	}

	/*
	 * Parsers
	 */
	public <C, T extends ParseResult, S extends ParseResultExpectation<T>, P extends AbstractParser<C, T, S>> P createParser(Class<P> parserClass) throws InternalErrorException {
		try {
			Constructor<P> constructor = parserClass.getConstructor(ParserToolbox.class);
			return constructor.newInstance(this);
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new InternalErrorException("Creating parser failed: " + e.getMessage(), e);
		}
	}

	public AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation> createExpressionParser() {
		return createExpressionParser(BinaryOperator.MAX_BINARY_OPERATOR_PRECEDENCE_LEVEL);
	}

	public AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation> createExpressionParser(int maxOperatorPrecedenceLevelToConsider) {
		return new dd.kms.zenodot.impl.parsers.ExpressionParser(this, maxOperatorPrecedenceLevelToConsider);
	}
}

package dd.kms.zenodot.impl.common;

import dd.kms.zenodot.api.common.ExtensionMemberProvider;
import dd.kms.zenodot.api.common.ExtensionMethodBody;
import dd.kms.zenodot.api.common.GeneralizedMethod;
import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodot.api.settings.extensions.ExtensionMethodDescription;
import dd.kms.zenodot.framework.utils.ParserToolbox;
import dd.kms.zenodot.impl.debug.ParserLoggers;
import dd.kms.zenodot.impl.utils.ClassUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExtensionMemberProviderImpl implements ExtensionMemberProvider
{
	private final ParserToolbox	parserToolbox;

	public ExtensionMemberProviderImpl(ParserToolbox parserToolbox) {
		this.parserToolbox = parserToolbox;
	}

	@Override
	public List<GeneralizedMethod> getExtensionMethodsFor(Class<?> clazz) {
		ParserLogger logger = parserToolbox.getSettings().getLogger();
		String className = clazz.getName();

		List<GeneralizedMethod> extensionMethods = new ArrayList<>();
		List<ExtensionMethodDescription> descriptions = parserToolbox.getSettings().getExtensionMethodDescriptions();
		for (ExtensionMethodDescription description : descriptions) {
			String name = description.getName();
			String declaringClassName = description.getDeclaringClassName();
			String extensionMethodClassName;
			try {
				extensionMethodClassName = ClassUtils.normalizeClassName(declaringClassName);
			} catch (ClassNotFoundException e) {
				logger.log(ParserLoggers.createLogEntry(LogLevel.WARNING, "Extension member provider", "Declaring class " + declaringClassName + " of extension method " + name + " not found."));
				continue;
			}
			if (!Objects.equals(extensionMethodClassName, className)) {
				continue;
			}
			String returnTypeName = description.getReturnTypeName();
			Class<?> returnType;
			try {
				String normalizedReturnTypeName = ClassUtils.normalizeClassName(returnTypeName);
				returnType = Class.forName(normalizedReturnTypeName);
			} catch (ClassNotFoundException e) {
				logger.log(ParserLoggers.createLogEntry(LogLevel.WARNING, "Extension member provider", "Return type " + returnTypeName + " of extension method " + name + " not found."));
				continue;
			}
			List<String> parameterTypeNames = description.getParameterTypeNames();
			int numParameters = parameterTypeNames.size();
			Class<?>[] parameterTypes = new Class<?>[numParameters];
			boolean validParameterTypes = true;
			for (int i = 0; i < numParameters; i++) {
				String parameterTypeName = parameterTypeNames.get(i);
				try {
					String normalizedParameterTypeName = ClassUtils.normalizeClassName(parameterTypeName);
					parameterTypes[i] = Class.forName(normalizedParameterTypeName);
				} catch (ClassNotFoundException e) {
					logger.log(ParserLoggers.createLogEntry(LogLevel.WARNING, "Extension member provider", "Parameter type " + parameterTypeName + " of extension method " + name + " not found."));
					validParameterTypes = false;
					break;
				}
			}
			if (!validParameterTypes) {
				continue;
			}

			String[] parameterNames = description.getParameterNames().toArray(new String[0]);
			boolean varArgs = description.isVarArgs();
			ExtensionMethodBody methodBody = new ExtensionMethodBodyImpl(clazz, parameterTypes, parameterNames, varArgs, description.getImplementationExpression(), parserToolbox);

			GeneralizedMethod extensionMethod = GeneralizedMethod.createExtensionMethod(clazz, name, description.getModifiers(), returnType, parameterTypes, varArgs, methodBody);
			extensionMethods.add(extensionMethod);
		}
		return extensionMethods;
	}
}

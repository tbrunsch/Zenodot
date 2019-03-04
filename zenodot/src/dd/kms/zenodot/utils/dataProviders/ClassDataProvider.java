package dd.kms.zenodot.utils.dataProviders;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import dd.kms.zenodot.ParserToolbox;
import dd.kms.zenodot.common.ReflectionUtils;
import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.matching.MatchRatings;
import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.matching.TypeMatch;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.settings.Imports;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ClassUtils;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.wrappers.ClassInfo;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassDataProvider
{
	private static final Map<String, Class<?>>	PRIMITIVE_CLASSES_BY_NAME	= ReflectionUtils.getPrimitiveClasses().stream()
		.collect(Collectors.toMap(
				Class::getName,
				clazz -> clazz
				)
		);
	private static final List<ClassInfo>		PRIMITIVE_CLASS_INFOS		= PRIMITIVE_CLASSES_BY_NAME.keySet().stream().map(ClassInfo::forNameUnchecked).collect(Collectors.toList());

	private static final ClassPath				CLASS_PATH;
	private static final Set<String>			TOP_LEVEL_CLASS_NAMES;
	private static final Set<String>			PACKAGE_NAMES;

	static {
		ClassPath classPath;
		Set<String> topLevelClassNames;
		try {
			classPath = ClassPath.from(ClassLoader.getSystemClassLoader());
			topLevelClassNames = classPath.getTopLevelClasses()
				.stream()
				.map(ClassPath.ClassInfo::getName)
				.collect(Collectors.toSet());
		} catch (IOException e) {
			classPath = null;
			topLevelClassNames = ImmutableSet.of();
		}
		CLASS_PATH = classPath;

		TOP_LEVEL_CLASS_NAMES = topLevelClassNames;

		Set<String> packageNames = new LinkedHashSet<>();
		for (String className : TOP_LEVEL_CLASS_NAMES) {
			for (String packageName = ClassUtils.getParentPath(className); packageName != null; packageName = ClassUtils.getParentPath(packageName)) {
				packageNames.add(packageName);
			}
		}
		PACKAGE_NAMES = ImmutableSet.copyOf(packageNames);
	}

	private final ParserToolbox parserToolbox;
	private final Imports		imports;

	public ClassDataProvider(ParserToolbox parserToolbox) {
		this.parserToolbox = parserToolbox;
		this.imports = parserToolbox.getSettings().getImports();
	}

	public ParseResultIF readClass(TokenStream tokenStream) {
		ClassReader reader = new ClassReader(parserToolbox, imports, tokenStream);
		return reader.read();
	}

	public ParseResultIF readInnerClass(TokenStream tokenStream, TypeInfo contextType) {
		Class<?> contextClass = contextType.getRawType();
		int startPosition = tokenStream.getPosition();

		if (tokenStream.isCaretAtPosition()) {
			return suggestInnerClasses("", contextClass, startPosition, startPosition);
		}

		Token identifierToken;
		try {
			identifierToken = tokenStream.readIdentifier();
		} catch (TokenStream.JavaTokenParseException e) {
			return new ParseError(startPosition, "Expected inner class name", ParseError.ErrorType.WRONG_PARSER);
		}

		String innerClassName = identifierToken.getValue();

		if (identifierToken.isContainsCaret()) {
			return suggestInnerClasses(innerClassName, contextClass, startPosition, tokenStream.getPosition());
		}

		Optional<Class<?>> firstClassMatch = Arrays.stream(contextClass.getDeclaredClasses())
			.filter(clazz -> clazz.getSimpleName().equals(innerClassName))
			.findFirst();
		if (!firstClassMatch.isPresent()) {
			return new ParseError(startPosition, "Unknown inner class '" + innerClassName + "'", ParseError.ErrorType.WRONG_PARSER);
		}

		Class<?> innerClass = firstClassMatch.get();
		TypeInfo innerClassType = contextType.resolveType(innerClass);
		return new ClassParseResult(tokenStream.getPosition(), innerClassType);
	}

	private CompletionSuggestions suggestInnerClasses(String expectedName, Class<?> contextClass, int insertionBegin, int insertionEnd) {
		List<ClassInfo> classesToConsider = Arrays.stream(contextClass.getDeclaredClasses())
											.map(clazz -> ClassInfo.forNameUnchecked(clazz.getName()))
											.collect(Collectors.toList());
		Map<CompletionSuggestionIF, MatchRating> ratedSuggestions = ParseUtils.createRatedSuggestions(
				classesToConsider,
				classInfo -> new CompletionSuggestionClass(classInfo, insertionBegin, insertionEnd),
				rateClassFunc(expectedName)
		);
		return new CompletionSuggestions(insertionBegin, ratedSuggestions);
	}

	private static class ClassReader
	{
		private final ParserToolbox parserToolbox;
		private final Imports 		imports;
		private final TokenStream	tokenStream;

		private String				packageOrClassName		= "";
		private int					identifierStartPosition	= -1;

		ClassReader(ParserToolbox parserToolbox, Imports imports, TokenStream tokenStream) {
			this.parserToolbox = parserToolbox;
			this.imports = imports;
			this.tokenStream = tokenStream;
		}

		ParseResultIF read() {
			while (true) {
				identifierStartPosition = tokenStream.getPosition();
				Token packageOrClassToken;
				try {
					packageOrClassToken = tokenStream.readPackageOrClass();
				} catch (TokenStream.JavaTokenParseException e) {
					return new ParseError(identifierStartPosition, "Expected sub-package or class name", ParseError.ErrorType.SYNTAX_ERROR);
				}
				packageOrClassName += packageOrClassToken.getValue();
				if (packageOrClassToken.isContainsCaret()) {
					return suggestClassesAndPackages(identifierStartPosition, tokenStream.getPosition(), packageOrClassName);
				}

				Class<?> detectedClass = detectClass(packageOrClassName);
				if (detectedClass != null) {
					return new ClassParseResult(tokenStream.getPosition(), TypeInfo.of(detectedClass));
				}

				Token characterToken = tokenStream.readCharacterUnchecked();
				if (characterToken == null ||  characterToken.getValue().charAt(0) != '.') {
					return new ParseError(identifierStartPosition, "Unknown class name '" + packageOrClassName + "'", ParseError.ErrorType.SEMANTIC_ERROR);
				}

				if (!packageExists(packageOrClassName)) {
					return new ParseError(tokenStream.getPosition(), "Unknown class or package '" + packageOrClassName + "'", ParseError.ErrorType.SEMANTIC_ERROR);
				}

				packageOrClassName += ".";
				if (characterToken.isContainsCaret()) {
					return suggestClassesAndPackages(tokenStream.getPosition(), tokenStream.getPosition(), packageOrClassName);
				}
			}
		}

		private static boolean packageExists(String packageName) {
			String packagePrefix = packageName + ".";
			return TOP_LEVEL_CLASS_NAMES.stream().anyMatch(name -> name.startsWith(packagePrefix));
		}

		private Map<CompletionSuggestionIF, MatchRating> suggestUnqualifiedClasses(String classPrefix, int insertionBegin, int insertionEnd, Set<ClassInfo> classes, Set<ClassInfo> suggestedClasses) {
			if (ClassUtils.lastIndexOfPathSeparator(classPrefix) >= 0) {
				// class is fully qualified, so no match
				return ImmutableMap.of();
			}
			Sets.SetView<ClassInfo> newSuggestedClasses = Sets.difference(classes, suggestedClasses);
			classes.addAll(newSuggestedClasses);
			return ParseUtils.createRatedSuggestions(
				newSuggestedClasses,
				classInfo -> new CompletionSuggestionClass(classInfo, insertionBegin, insertionEnd),
				rateClassFunc(classPrefix)
			);
		}

		private static Map<CompletionSuggestionIF, MatchRating> suggestQualifiedClasses(String classPrefixWithPackage, int insertionBegin, int insertionEnd, Set<ClassInfo> suggestedClasses) {
			String packageName = ClassUtils.getParentPath(classPrefixWithPackage);
			if (packageName == null) {
				// class is not fully qualified, so no match
				return ImmutableMap.of();
			}
			String prefix = packageName + ".";
			int lastSeparatorIndex = packageName.length();
			List<ClassInfo> newSuggestedClasses = new ArrayList<>();
			for (String className : TOP_LEVEL_CLASS_NAMES) {
				if (ClassUtils.lastIndexOfPathSeparator(className) != lastSeparatorIndex) {
					continue;
				}
				if (!className.startsWith(prefix)) {
					continue;
				}
				ClassInfo clazz = ClassInfo.forNameUnchecked(className);
				if (suggestedClasses.contains(clazz)) {
					continue;
				}
				newSuggestedClasses.add(clazz);
			}
			suggestedClasses.addAll(newSuggestedClasses);
			String classPrefix = ClassUtils.getLeafOfPath(classPrefixWithPackage);
			return ParseUtils.createRatedSuggestions(
				newSuggestedClasses,
				classInfo -> new CompletionSuggestionClass(classInfo, insertionBegin, insertionEnd),
				rateClassFunc(classPrefix)
			);
		}

		private static Map<CompletionSuggestionIF, MatchRating> suggestPackages(String packagePrefix, int insertionBegin, int insertionEnd) {
			String parentPackage = ClassUtils.getParentPath(packagePrefix);
			int lastSeparatorIndex = ClassUtils.lastIndexOfPathSeparator(packagePrefix);
			List<String> suggestedPackageNames = new ArrayList<>();
			for (String packageName : PACKAGE_NAMES) {
				if (ClassUtils.lastIndexOfPathSeparator(packageName) != lastSeparatorIndex) {
					continue;
				}
				if (parentPackage != null && !packageName.startsWith(parentPackage)) {
					continue;
				}
				suggestedPackageNames.add(packageName);
			}
			String subpackagePrefix = ClassUtils.getLeafOfPath(packagePrefix);
			return ParseUtils.createRatedSuggestions(
				suggestedPackageNames,
				packageName -> new CompletionSuggestionPackage(packageName, insertionBegin, insertionEnd),
				ratePackageFunc(subpackagePrefix)
			);
		}

		private CompletionSuggestions suggestClassesAndPackages(int insertionBegin, int insertionEnd, String classOrPackagePrefix) {
			ImmutableMap.Builder<CompletionSuggestionIF, MatchRating> suggestionBuilder = ImmutableMap.builder();

			Set<ClassInfo> importedClasses = getImportedClasses();
			Set<String> importedPackageNames = getImportedPackageNames();
			Set<ClassInfo> topLevelClassesInPackages = getTopLevelClassesInPackages(importedPackageNames);

			Set<ClassInfo> suggestedClasses = new HashSet<>();

			suggestionBuilder.putAll(suggestUnqualifiedClasses(classOrPackagePrefix, insertionBegin, insertionEnd, importedClasses, suggestedClasses));
			suggestionBuilder.putAll(suggestUnqualifiedClasses(classOrPackagePrefix, insertionBegin, insertionBegin, topLevelClassesInPackages, suggestedClasses));
			suggestionBuilder.putAll(suggestQualifiedClasses(classOrPackagePrefix, insertionBegin, insertionEnd, suggestedClasses));
			suggestionBuilder.putAll(suggestPackages(classOrPackagePrefix, insertionBegin, insertionEnd));

			return new CompletionSuggestions(insertionBegin, suggestionBuilder.build());
		}

		private Class<?> detectClass(String className) {
			return Stream.of(
						PRIMITIVE_CLASSES_BY_NAME.get(className),
						getClassImportedViaClassName(className),
						getClassImportedViaPackage(className),
						ClassUtils.getClassUnchecked(className)
					).filter(Objects::nonNull)
					.findFirst().orElse(null);
		}

		private Class<?> getClassImportedViaClassName(String className) {
			for (ClassInfo importedClass : getImportedClasses()) {
				String unqualifiedName = importedClass.getUnqualifiedName();
				if (className.equals(unqualifiedName) || className.startsWith(unqualifiedName + ".")) {
					// Replace simpleName by fully qualified imported name and replace '.' by '$' when separating inner classes
					String fullyQualifiedClassName = importedClass.getNormalizedName()
							+ className.substring(unqualifiedName.length()).replace('.', '$');
					return ClassUtils.getClassUnchecked(fullyQualifiedClassName);
				}
			}
			return null;
		}

		private Class<?> getClassImportedViaPackage(String className) {
			return getImportedPackageNames().stream()
					.map(packageName -> packageName + "." + className)
					.map(ClassUtils::getClassUnchecked)
					.filter(Objects::nonNull)
					.findFirst().orElse(null);
		}

		private Class<?> getThisClass() {
			ObjectInfo thisInfo = parserToolbox.getThisInfo();
			TypeInfo thisType = parserToolbox.getObjectInfoProvider().getType(thisInfo);
			return thisType.getRawType();
		}

		private Set<ClassInfo> getImportedClasses() {
			Set<ClassInfo> importedClasses = new LinkedHashSet<>();
			importedClasses.addAll(PRIMITIVE_CLASS_INFOS);
			Class<?> thisClass = getThisClass();
			if (thisClass != null) {
				importedClasses.add(ClassInfo.forNameUnchecked(thisClass.getName()));
			}
			importedClasses.addAll(imports.getImportedClasses());
			return importedClasses;
		}

		private Set<String> getImportedPackageNames() {
			Set<String> importedPackageNames = new LinkedHashSet<>();
			Class<?> thisClass = getThisClass();
			if (thisClass != null) {
				importedPackageNames.add(thisClass.getPackage().getName());
			}
			importedPackageNames.add("java.lang");
			importedPackageNames.addAll(imports.getImportedPackageNames());
			return importedPackageNames;
		}

		private Set<ClassInfo> getTopLevelClassesInPackages(Collection<String> packageNames) {
			Set<ClassInfo> classes = new HashSet<>();
			for (String packageName : Iterables.filter(packageNames, Objects::nonNull)) {
				for (ClassPath.ClassInfo classInfo : CLASS_PATH.getTopLevelClasses(packageName)) {
					classes.add(ClassInfo.forNameUnchecked(classInfo.getName()));
				}
			}
			return classes;
		}
	}

	/*
	 * Class Suggestions
	 */
	private static StringMatch rateClassByName(ClassInfo classInfo, String expectedSimpleClassName) {
		return MatchRatings.rateStringMatch(classInfo.getUnqualifiedName(), expectedSimpleClassName);
	}

	private static Function<ClassInfo, MatchRating> rateClassFunc(final String simpleClassName) {
		return classInfo -> new MatchRating(rateClassByName(classInfo, simpleClassName), TypeMatch.NONE);
	}

	public static String getClassDisplayText(ClassInfo classInfo) {
		return classInfo.getNormalizedName();
	}

	/*
	 * Package Suggestions
	 */
	private static StringMatch ratePackageByName(String packageName, String expectedName) {
		int lastDotIndex = packageName.lastIndexOf('.');
		String subpackageName = packageName.substring(lastDotIndex + 1);
		return MatchRatings.rateStringMatch(subpackageName, expectedName);
	}

	private static Function<String, MatchRating> ratePackageFunc(String expectedName) {
		return packageName -> new MatchRating(ratePackageByName(packageName, expectedName), TypeMatch.NONE);
	}
}
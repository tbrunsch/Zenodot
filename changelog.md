# Changelog

## v0.4.0

API changes:

  - `CodeCompletion`: Replaced method `getInsertionRange()` by the methods `getInsertionBegin()` and `getInsertionEnd()`.
  - `CodeCompletionClass.getClassInfo()` now returns a `ClassInfo` instead of a `Class` to avoid loading classes unnecessarily.

## v0.3.0

  - A new evaluation mode has been added that has some advantages of dynamic typing but prevents side effects during code completion and when the expression evaluation cannot be evaluated correctly.
  
  - Support for lambdas has been added with the following restrictions: Types of generic parameters cannot be inferred, so generic parameters have to be cast, and method references are currently not supported.
  
  - Added possibility to specify a functional interface and the parameter types of its method for parsing lambdas. This can be done via `Parsers.createExpressionParserBuilder()` and `ExpressionParserBuilder.createLambdaParser()`. 
  
  - Added method `Parsers.preloadClasses()` that allows loading classes before they are needed.
  
  - Since variables are not part of the `ParserSettings` anymore, but optionally specified when an `ExpressionParser` is created, Zenodot can now overwrite variables (when they are not declared `final`).
  
API changes:

  - Dynamic typing is now not enabled/disabled via `ParserSettingsBuilder.enableDynamicTyping()`. Instead, you now specify the evaluation mode via `ParserSettingsBuilder.evaluationMode()`. The new evaluation mode is now the default.
  
  - The limited support for generic type inference via Guava's `TypeToken` has been removed completely because the benefit turned out to be negligible and the feature made the code more complex. Particularly, the class `TypeInfo`, that essentially was a wrapper around `TypeToken`, has been removed and replaces by `Class<?>`.
  
  - Since we removed generic support, the class `ObjectInfo` does not carry that much information anymore. Therefore we decided to remove this interface from the API and work with `Object` instead. This makes the API much easier to read and use. As a consequence, the result of an expression evaluation is always an rvalue. You cannot use it as context of a later expression evaluation and assign it a value there. We consider this a mild restriction because this is no important use case.
  
  - Together with `ObjectInfo` we have also abandoned all other wrapper classes from the API. This lead to a major simplification of the API.
  
  - The method `CodeCompletionVariable.getVariable()` has been replaced by the method `CodeCompletionVariable.getVariableName()` which returns a `String` instead of a `Variable`.
  
  - We have replaced the custom interface `IntRange` by Guava's class `Range`.
  
  - Variables are no part of the `ParserSettings` anymore. Instead, you can specify them when creating an `ExpressionParser` via `Parsers.createExpressionParser()`. This allows Zenodot to modify existing and potentially introduce new variables.
  
  - The class `Field` has been replaced by the interface `GeneralizedField` that can also represent the field `length` of arrays.

## v0.2.2

  - Fixed several problems when dynamic typing and expression compilation are combined
  - ObjectInfoProvider now provides a method getRuntimeType() to determine the runtime type of an object given its declared type

## v0.2.1

  - `FieldScanner` and `MethodScanner` now also support searching only non-static fields and methods
  - `ConstructorScanner`, `FieldScanner`, and `MethodScanner` are now immutable and are created via builders

API changes:

  - `ConstructorScanner`, `FieldScanner`, and `MethodScanner` now have to be instantiated via builder classes `ConstructorScannerBuilder`, `FieldScannerBuilder`, and `MethodScannerBuilder`

## v0.2.0

  - refactored and simplified internal workflow to minimize boilerplate code
  - merged expression compiler and expression parser into one class
  - expression compilation is now also available with dynamic typing enabled
  - increased priority of wildcard matches for code completions
  - `MatchRating` now provides a method to obtain an integer representation
  - support calling generic methods like `Arrays.asList()`
  - support `instanceof` operator
  - extended documentation
  - added code samples
  - added technical documentation `TechDoc.md` describing key concepts
  - added changelog
  
API changes:

  - changed package structure: all API classes are now in the package `dd.kms.zenodot.api`
  - `ParserSettingsBuilder`s are now created via the method `ParserSettingsBuilder.create()` 
  - renamed `CompletionSuggestion` to `CodeCompletion` (same for corresponding package)
  - `CodeCompletion`s now provide a method `getRating()` that returns their rating
  - replaced method `MatchRating.getAccessMatch()` by method `MatchRating.isAccessDiscouraged()`
  - merged `ExpressionCompiler` into `ExpressionParser`
  - `ExpressionParser.compile()` now expects an `ObjectInfo` instead of a `TypeInfo` (see JavaDoc for details)
  - renamed methods `suggestCodeCompletion()` to `getCompletions()`
  - method `getCompletions()` now returns a list of completions rather than a map from completions to ratings
  - replaced enum `EvaluationMode` by simple `boolean` flag "evaluate"  

## v0.1.1

  - optionally suggest qualified class names when typing unqualified class names
  - several performance improvements

## v0.1.0

First Zenodot release
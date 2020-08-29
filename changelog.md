# Changelog

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
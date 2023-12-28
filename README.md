# Zenodot
Zenodot is a Java library for parsing Java expressions. Notable features are:

  - Name and type based code completion
  - Optional dynamically typed expression evaluation
  - Parsing of custom variables
  - Support for adding additional parsers for syntax extensions

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
# Table of Contents

- [Target](#target)
- [When not to use Zenodot](#when-not-to-use-zenodot)
- [Example](#example)
- [Features and Short Comings](#features-and-short-comings)
  - [Code Completion](#code-completion)
  - [Dynamic Typing](#dynamic-typing)
  - [Lambdas](#lambdas)
  - [Custom Variables](#custom-variables)
  - [Syntax Extensions](#syntax-extensions)
  - [Operators](#operators)
- [Evaluation Context](#evaluation-context)
- [Handling Code Completions](#handling-code-completions)
- [Parser Settings](#parser-settings)
  - [Dynamic Typing Example](#dynamic-typing-example)
- [Open Source License Acknowledgement](#open-source-license-acknowledgement)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# Target
Zenodot has been developed to complement the traditional IDE-based debugging. The traditional debugging steps are as follows:

  1. Set a breakpoint at an appropriate position in the source code
  1. Define a condition for that breakpoint to reduce the number of irrelevant stops
  1. Trigger an event in the application to make the debugger stop at that breakpoint
  1. Evaluate an expression in the desired context

While this kind of debugging is very powerful with a modern IDE, it can be a bit frustrating to perform all these steps just to recover the object in the debugging process one has already found in the application.

When an application integrates the Zenodot library, then an alternative workflow will look as follows:

  1. Find the object in the application
  1. Open the UI that is linked with the library
  1. Enter the expression you want to evaluate

Note that you do not have to switch to your IDE at all. In particular, you can (to some extent) debug your application without an IDE.

# When not to use Zenodot

You should not use Zenodot if at least one of the following applies to you:

  - You need a full-blown Java parser that can parse whole code fragments.
  - You want to evaluate expressions as fast as possible.
  - You are worried about security in general and code injection in particular.
  - You do not trust a parser library written by someone inexperienced in compiler construction.

# Example

The following example, taken from **SimpleExpressionSample.java**, shows how to evaluate an expression in Zenodot:

```
ParserSettings settings = ParserSettingsBuilder.create().build();
String expression = "Math.max(new int[]{ 2+3, 123/3, 1 << 4 }[1], (int) Math.round(2.718E2))";
ExpressionParser parser = Parsers.createExpressionParser(settings);
System.out.println("Result: " + parser.evaluate(expression, null));
```

Let us have a closer look at all steps:

  1. Configure settings for the parser: There are various options to configure the parser. See [Parser Settings](#parser-settings) for details. In this simple example we rely on the default settings. 
  1. Create the expression parser: Parsers are created via the utility class `Parsers`. This class can also be used to create parsers for classes and packages. 
  1. Define which expression to evaluate: In most cases, the expression will be given by the user. In this example we hard-coded it.
  1. Evaluate the expression in a certain context. This context describes within which instance we pretend to be when evaluating an expression. This is why the context is often referred to by `thisValue` in the API. If the context is the String "Zenodot", then the expressions "this.substring(4)" or simply "substring(4)" will return "dot" (cf. **ExpressionContextSample.java**). If the context was a list, then we would get a `ParseException` when evaluating this expression. Since the expression in the sample can be evaluated statically without any context, we specify the null literal as context. See [Evaluation Context](#evaluation-context) for details.

# Features and Short Comings

Instead of listing all things that work as expected we will highlight positive and negative points that deviate from the expectations of a regular Java parser.

## Code Completion

If you only want to execute Java code (or at least something similar), then you can use [Groovy](http://www.groovy-lang.org/). It is much more powerful and probably also much more reliable. However, if you need code completion when writing a single expression, then Zenodot might be the right choice. Zenodot provides code completions for packages, classes, methods, and fields. The completions are rated according to a rating function that considers names and types. The rating algorithm supports camel case pattern matching, i.e., the completion "NullPointerException" gets a high rating if the search text is "NuPoEx". 

Note that the completions returned by the parsers are unsorted since it is the caller's responsibility to decide how to order them. However, the utility class `Parsers` provides a comparator `Parsers.COMPLETION_COMPARATOR` that is used in all Zenodot unit tests and that might yield acceptable results in several other use cases.

## Dynamic Typing

When inspecting the internal state of an object returned by a method, you sometimes have to cast it to its runtime type to be able to access its methods because they are not published via the declared type. To avoid such casts, you can activate dynamic typing. If this option is selected, then both, code completion and expression evaluation, ignore the declared type and use the runtime type of an object instead (see [Dynamic Typing Example](#dynamic-typing-example) for an example). Although this can be handy in some cases, you should be aware of the risks. If you call a method with side effects, then this side effect will also be triggered when requesting code completions or when evalutating an expression with a syntax error. Furthermore, method overloads can be resolved differently with static and dynamic typing.

Zenodot also provides a hybrid between static and dynamic typing that avoids such side effects but provides all benefits of dynamic typing except for determining the return type of a method. This mode is called `EvaluationMode.MIXED`.

## Lambdas

Zenodot supports parsing lambdas as the following sample, taken from **LambdaSample.java**, shows:

```
ParserSettings settings = ParserSettingsBuilder.create()
	.importPackages(Collections.singletonList("java.util"))
	.build();
// s must be cast to String because Zenodot does not infer generic types
String expression = "Arrays.asList(\"1\", \"2\", \"3\").stream().mapToInt(s -> Integer.parseInt((String) s)).sum()";
ExpressionParser parser = Parsers.createExpressionParser(settings);
System.out.println("Result: " + parser.evaluate(expression, null));
```

Since Zenodot does not support type inference, parameters for generic types have to be cast. In the example above, the parameter `s` has to be cast to `String`.

Another restriction is that Zenodot does currently not support method references or lambdas with code blocks.

It is also possible to create a parser particularly for parsing lambdas for a specific functional interface. For this, you have to create an expression parser for this specific use case:

 1. Create an `ExpressionParserBuilder` via `Parsers.createExpressionParserBuilder()`
 1. Create a lambda parser via `ExpressionParserBuilder.createLambdaParser()` for the desired functional interface. There you can optionally specify the parameter types for your use case. This is necessary if the interface is generic or extends a generic interface and you want to avoid casts in the lambda expression.

The following sample, taken from **LambdaParserSample.java**, shows how to create a lambda parser:

```
ParserSettings settings = ParserSettingsBuilder.create().build();

// create a lambda parser for Comparator<String> where compare() takes two String parameters
LambdaExpressionParser<Comparator> parser = Parsers.createExpressionParserBuilder(settings)
    .createLambdaParser(Comparator.class, String.class, String.class);

// create a comparator that compares strings by considering them as numbers
String expression = "(s1, s2) -> Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2))";
Comparator<String> comparator = parser.evaluate(expression, null);

// sort strings by considering them as numbers
List<String> numbersAsStrings = Arrays.asList("123", "42", "0", "99");
numbersAsStrings.sort(comparator);
System.out.println(numbersAsStrings);
``` 

## Custom Variables

Zenodot allows you to declare variables that can be set and accessed in expressions. This can save some typing when repeatedly evaluating expressions in a certain context.

To specify variables when parsing expressions, you have to do the following steps:

  1. Create a collection of variables via `Variables.create()`.
  1. Add variables to this collection via `Variables.createVariable()`. There you can specify whether the variable is `final` or not.
  1. Create an expression parser builder via `Parser.createExpressionParserBuilder()`.
  1. Set the variables via `ExpressionParserBuilder.variables()`
  1. Create an expression parser via `ExpressionParserBuilder.createExpressionParser()`.

The following sample is an excerpt from **VariableSample**:

```
ParserSettings settings = ParserSettingsBuilder.create().build();
Variables variables = Variables.create()
	.createVariable("i", 42, true)
	.createVariable("x", 3.14, false)
ExpressionParser parser = Parsers.createExpressionParserBuilder(settings)
	.variables(variables)
	.createExpressionParser();

System.out.println(parser.evaluate("i", null));	    // prints 42
parser.evaluate("x = 2.72", null);                  // sets x to 2.72
System.out.println(parser.evaluate("x", null));     // prints 2.72
```

## Extensions

Zenodot can be extended in different ways that we will discuss in the next subsections. The following extensions are part of the Zenodot project, but must be configured and activated manually:

* Custom Hierarchy Parser: An additional parser that helps to navigate within custom tree structures when typing expressions.
* Directory Completions: Provides code completions for String literals in file system related methods and constructors like `new File(String)` or `Paths.get(String, String...)`.

### Additional Parsers

Zenodot provides a way to specify additional parsers that will be used for parsing expressions. This allows users to extend the Java syntax that is supported by the basic Zenodot parser. In this section we briefly describe how to do so:

1. You have to use an existing additional parser or write your own parser that extends `dd.kms.zenodot.framework.parsers.AbstractParser`.
2. Create an instance of `AdditionalParserSettings` that references, among others, the class and specific settings of that parser.
3. Register this instance via `ParserSettingsBuilder.additionalParserSettings()`.

While the Zenodot API has been kept stable as possible over time, the framework for writing parsers has been considered an internal part of Zenodot until recently. Although many thoughts went into that framework - after all, we had to write quite some parsers with that framework -, it is far from perfect and, hence, more likely to be subject to change. Currently, we consider it more a framework for providing our own additional parsers that are not meant to be part of the basic Zenodot parser, but that could additionally be loaded by users. As of now, we discourage you to write your own parsers with that framework.

The `Custom Hierarchy Parser` (one of the Zenodot modules) is an example of such an additional parser. This parser supports you when navigating through custom tree structures.

### Additional Code Completions

Zenodot does not provide any code completions for, e.g., String literals by default. However, it is possible to specify which code completions to provide for certain parameters of certain methods or constructors. The parser extension `Directory Completions` (one of the Zenodot modules) uses this feature to help you navigating a file system when typing String literals in, e.g., the constructor `new File(String)`.

## Operators

Zenodot implements most but not all unary and binary operators. The following operators are currently not supported:

  - Postfix increment (`++`) and decrement (`--`)
  - ternary operator `? :`
  - the operators `+=`, `-=`, `*=`, `/=`, `%=`, `<<=`, `>>=`, `>>>=`, `&=`, `^=`, and `|=`

# Evaluation Context

Expressions are evaluated in a certain context, also referred to as `thisValue` in the API. This context describes within which instance we pretend to be when evaluating the expression. This instance can referred to by the literal `this` in an expression and it can be omitted when accessing its fields or methods. Consider the following code taken from **ExpressionContextSample.java**:

```
ParserSettings settings = ParserSettingsBuilder.create().build();
String expression = "substring(4)";
ExpressionParser parser = Parsers.createExpressionParser(settings);
System.out.println("Result: " + parser.evaluate(expression, "Zenodot"));
``` 

The expression "substring(4)" is evaluated using the string "Zenodot" as context. It is equivalent to the expression "this.substring(4)" and evaluates to "dot". 

# Handling Code Completions

Code completions are represented by the interface `CodeCompletion`. We will discuss some of its methods in detail:

  - `getInsertionRange()` returns the range of the current text that should be replaced by the code completion.
  - `getTextToInsert()` returns the text that should be used to replace the range of the current text specified by `getInsertionBegin()` and `getInsertionEnd()`.  
  - `getCaretPositionAfterInsertion()` returns the position of the caret after inserting the code completion. In many cases it will be the end of the insertion range. For methods, however, it is the position after the opening parenthesis.
  - `toString()` returns the suggested text that should be displayed to the user. This is not always the same as the text returned by `getTextToInsert()`. For methods, for instance, it contains information about the argument types.

If the interface `CodeCompletion` does not provide sufficient information because you need to handle different types of code completions differently, then you can cast them to one of the following specific interfaces: `CodeCompletionClass`, `CodeCompletionPackage`, `CodeCompletionField`, `CodeCompletionMethod`, `CodeCompletionKeyword`, or `CodeCompletionVariable`.   

# Parser Settings

It is obligatory to create an instance of `ParserSettings` in order to create a parser. This instance is created via a `ParserSettingsBuilder`, which is returned by the factory method `ParserSettingsBuilder.create()`. Several options are available:

  - Completion mode: With the completion mode you can configure how code completions are generated. You can specify whether the whole word or only the word until the caret is considered for suggesting completions and whether only the word until the caret or the whole word is proposed to be replaced. Example: Consider the text "text" and assume that the caret is between "te" and "xt". Furthermore, assume that there are two potential completions "test" and "texture".
    * `CompletionMode.COMPLETE_AND_REPLACE_UNTIL_CARET`: The text "te" is completed. Both completions, "test" and "texture", are matching completions of "te". The completions suggest to replace the text "te". The new text would therefore be "testxt" or "texturext", respectively.
    * `CompletionMode.COMPLETE_UNTIL_CARET_REPLACE_WHOLE_WORDS`: As in the previous mode, the text "te" is completed and, hence, both completions, "test" and "texture", match. However, the completions suggest to replace the whole word "text", leading to "test" or "texture", respectively.
    * `CompletionMode.COMPLETE_AND_REPLACE_WHOLE_WORDS` (default): The whole word "text" is completed. Here, only the completion "texture" matches the text "text". This completion suggests to replace the whole word "text" by "texture".    

  - Class imports: Imported classes can be referenced by their simple instead of their fully qualified names from within expressions.
  
  - Package imports: All classes in imported packages can be referenced by their simple instead of their fully qualified names from within expressions.
  
  - Minimum access modifier: You can specify the minimum access modifier fields, methods, or constructors must have in order to be accessible by the parser. If you want to debug implementation details of objects, then you should set it to `AccessModifier.PRIVATE`. If you just want to call API methods, then it is advisable to set it to `AccessModifier.PUBLIC` (default). This may reduce the number of code completions significantly by filtering out irrelevant completions. Remarks:
    * For the sake of simplicity we decided that the accessibility of fields, methods, and constructors is independent of context the expression is evaluated in. If the minimum access modifier is `AccessModifier.PUBLIC`, then you cannot access protected fields, even if they are fields of the context.
    * The minimum access modifier is not considered when accessing classes. The reason is that we want to avoid loading classes only for determining their access modifier.

  - Evaluation mode: You can specify how expressions are evaluated: With static typing, with dynamic typing, or with a hybrid. By default, the hybrid is selected. See [Dynamic Typing](#dynamic-typing) for details and [Dynamic Typing Example](#dynamic-typing-example) for an example.
  
  - Consider all classes for class completions: You can specify whether all top-level classes are considered for class completions. By default, they are not. When this option is enabled, then you also get code completions for classes you have not imported even if you only enter their simple name. Note that this option may take some time because all top-level classes will be considered. Additionally, this option does not make importing classes obsolete:
    * Simple names of classes that have not been imported are not valid when evaluating expressions. They are just considered for code completions.
    * Simple names of classes that have not been imported will be completed to fully qualified class names. Example: If you have imported the package "java.util" and request code completions for the text "Li", then you will get a completion "List". If you have neither imported the package "java.util" nor the class "java.util.List", then you will get a completion "java.util.List" when requesting code completions for the text "Li" if this option is enabled.

## Dynamic Typing Example

**DynamicTypingSample.java:** Consider the test class

```
static class TestClass
{
    public Object getObject() { return "This is a string"; }
}
```

and the following parser code:

```
TestClass testInstance = new TestClass();

ParserSettings settings = ParserSettingsBuilder.create()
	.evaluationMode(EvaluationMode.DYNAMIC_TYPING)
	.build();
String expression = "getObject().length()";
ExpressionParser parser = Parsers.createExpressionParser(settings);
System.out.println("Result: " + parser.evaluate(expression, testInstance));
```

Without dynamic typing, Zenodot would throw a `ParseException` when evaluating the expression "getObject().length()" because `getObject()` is declared to return an `Object`, which does not provide a method "length". With dynamic typing, Zenodot evaluates the subexpression "getObject()" and detects that the runtime type is `String`, which has a method "length". It then calls this method on the String "This is a string".

# Open Source License Acknowledgement

Zenodot utilizes [Guava: Google Core Libraries for Java](https://github.com/google/guava). This library is licensed under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

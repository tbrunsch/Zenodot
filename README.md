# Zenodot
Zenodot is a Java library for parsing Java expressions. Notable features are:

  - Name and type based code completion
  - Optional dynamically typed expression evaluation
  - Parsing of custom variables
  - Parsing of individual hierarchies that are not reflected by regular Java syntax

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
  - [Custom Hierarchies](#custom-hierarchies)
  - [Operators](#operators)
- [Evaluation Context](#evaluation-context)
- [Handling Code Completions](#handling-code-completions)
- [Parser Settings](#parser-settings)
  - [Dynamic Typing Example](#dynamic-typing-example)
  - [Custom Hierarchy Example](#custom-hierarchy-example)
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

Note that you do not have to switch to your IDE at all. In particular, you can (to some extend) debug your application without an IDE.

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

## Custom Hierarchies

One of the most interesting features of Zenodot is its support of custom hierarchies. If an application holds a dynamically created tree, then this tree can, of course, be traversed with an arbitrary Java parser. However, in many cases the traversed nodes are only represented by generic node classes. Only in rare cases there will be one node class for each node. Consequently, when traversing such a tree, you will have to call generic methods like `getChild(childIndex)` or something similar. This is different than what you see in your application where every node is displayed with its individual name. When restricted to Java syntax, you can not hope for meaningful code completion here. You have to deal with child indices instead.

Zenodot extends the Java syntax to allow for parsing a custom hierarchy. The only thing the application developer has to do is to specify his tree in a form Zenodot understands by implementing a certain interface. A user can then traverse that tree using the node names.

**Example:** Assume that you have a document viewer application that has loaded this document. It might store the content in a hierarchy with sections on the first level, subsections on the second level, and so one. Let us assume that we want to evaluate the object behind this section, i.e., the section "Features and Short Comings" -> "Custom Hierarchies". A classic approach would be to call `getSection(4).getSubsection(3)`. As you can imagine, handling the indices, which is only a technical detail, will become troublesome in large trees. However, if you have configured Zenodot correctly, then you can also write `{Features and Short Comings#Custom Hierarchies}` to reference the same node in your tree. This is much more readable and less error-prone. (Also note the spaces inside the node names.) Furthermore, you can, e.g., request code completion after typing `{Features and Short Comings#Custom`. The result will be `Custom Variables` and `Custom Hierarchies`.

See [Custom Hierarchy Example](#custom-hierarchy-example) to see how to use custom hierarchies in Zenodot.
 
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
  - `getTextToInsert()` returns the text that should be used to replace the range of the current text specified by`getInsertionRange()`.  
  - `getCaretPositionAfterInsertion()` returns the position of the caret after inserting the code completion. In many cases it will be the end of the insertion range. For methods, however, it is the position after the opening parenthesis.
  - `toString()` returns the suggested text that should be displayed to the user. This is not always the same as the text returned by `getTextToInsert()`. For methods, for instance, it contains information about the argument types.

If the interface `CodeCompletion` does not provide sufficient information because you need to handle different types of code completions differently, then you can cast them to one of the following specific interfaces: `CodeCompletionClass`, `CodeCompletionPackage`, `CodeCompletionField`, `CodeCompletionMethod`, `CodeCompletionKeyword`, `CodeCompletionVariable`, or `CodeCompletionObjectTreeNode`.   

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

  - Custom Hierarchy: You can specify the root of a custom hierarchy that can be accessed from within expressions by a dedicated syntax. See [Custom Hierarchies](#custom-hierarchies) for a motivation and [Custom Hierarchy Example](#custom-hierarchy-example) for an example. To make your custom hierarchy accessible for Zenodot, you must wrap all your nodes in custom implementations of `ObjectTreeNode`. You can use overloads of `ParserSettingsUtils.createLeafNode()` to wrap leaves of your hierarchy.  

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

## Custom Hierarchy Example

**CustomHierarchySample.java:* Assume that we have the hierarchy

  - numbers
    - pi (value = 3.14)
    - e (value = 2.72)
  - strings
    - short strings
      - test (value = "Test")
    - long strings
      - long string (value = "This is a long string.")
      - very long string (value = "This is a very long string.")

All leaves in this example have user objects (the "xxx" in "value = xxx"), but it is not uncommon that also inner nodes carry user objects. In order to make this hierarchy accessible for Zenodot, we have to represent it by a hiearchy of `ObjectTreeNode`s. Here we use simple utility methods

```
static ObjectTreeNode node(String name, ObjectTreeNode... childNodes) {
    return new ObjectTreeNode() {
        @Override
        public String getName() {
            return name;
        }

        @Override
        public Iterable<? extends ObjectTreeNode> getChildNodes() {
            return Arrays.asList(childNodes);
        }

        @Override
        public Object getUserObject() {
            return null;
        }
    };
}

static ObjectTreeNode leaf(String name, Object value) {
	return ParserSettingsUtils.createLeafNode(name, value);
}
```

which allows us to hard-code the hierarchy easily:

```
ObjectTreeNode root = node(null,
    node("numbers",
        leaf("pi", 3.14),
        leaf("e", 2.72)),
    node("strings",
        node("short strings",
            leaf("test", "Test")),
        node("long strings",
            leaf("long string", "This is a long string."),
            leaf("very long string", "This is a very long string.")
        )
    )
);
```

In real-life applications such hierarchies will by dynamic, so you will not hard-code them like in this example.

No consider the following parser code:

```
ParserSettings settings = ParserSettingsBuilder.create()
	.customHierarchyRoot(root)
	.build();
ExpressionParser parser = Parsers.createExpressionParser(settings);
String text = "{strings#long strings#ve";
List<CodeCompletion> completions = new ArrayList<>(parser.getCompletions(text, text.length(), null));
Collections.sort(completions, Parsers.COMPLETION_COMPARATOR);

System.out.println("Completion: " + completions.get(0).getTextToInsert());

String expression = "{numbers#e}";
Object result = parser.evaluate(expression, null);

System.out.println("Result: " + result);
```

The first part of the code requests Zenodot to suggest child nodes of the node for the path "strings" -> "long strings" considering the text "ve". The output will be "Completion: very long string". Note that node names can even contain white spaces. The only forbidden characters are '#' and '}'.

The second part of the code accesses the user object assigned to the node for the path "numbers" -> "e". The output is "Result: 2.72".

Observe that custom hierarchies are a language extension supported by Zenodot that allow semantic instead of syntactic code completions and expression evaluations. In contrast to class hierarchies that are determined at compile time (in most of the cases), custom hierarchies can be build and updated during an application's lifetime. 

# Open Source License Acknowledgement

Zenodot utilizes [Guava: Google Core Libraries for Java](https://github.com/google/guava). This library is licensed under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

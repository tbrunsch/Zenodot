# Zenodot
Zenodot is a Java library for parsing Java expressions. Notable features are:

  - Name and type based code completion
  - Optional dynamically typed expression evaluation
  - Parsing of custom variables
  - Parsing of individual hierarchies that are not reflected by regular Java syntax

# Target
Zenodot has been developed to complement the traditional IDE-based debugging. The traditional debugging steps are as follows:

  1. Set a break point at an appropriate position in the source code
  1. Define a condition for that break point to reduce the number of irrelevant stops
  1. Trigger an event in the application to make the debugger stop at that break point
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
ExpressionParser parser = Parsers.createExpressionParser(settings);
String expression = "Math.max(new int[]{ 2+3, 123/3, 1 << 4 }[1], (int) Math.round(2.718E2))";
System.out.println("Result: " + parser.evaluate(expression, InfoProvider.NULL_LITERAL).getObject());
```

Let us have a closer look at all steps:

  1. Configure settings for the parser: There are various options to configure the parser. See [Parser Settings](#parser-settings) for details. In this simple example we rely on the default settings. 
  1. Create the expression parser: Parsers are created via the utility class `Parsers`. This class can also be used to create parsers for classes and packages. 
  1. Define which expression to evaluate: In most cases, the expression will be given by the user. In this example we hard-coded it.
  1. Evaluate the expression in a certain context. This context describes within which instance we pretend to be when evaluating an expression. This is why the context is often referred to by `thisValue` in the API. If the context is the String "Zenodot", then the expressions "this.substring(4)" or simply "substring(4)" will return "dot" (cf. **ExpressionContextSample.java**). If the context was a list, then we would get a `ParseException` when evaluating this expression. Since the expression in the sample can be evaluated statically without any context, we specify the null literal as context. See [Evaluation Context](#evaluation-context) for details. Note that Zenodot operates on wrapper classes like `ObjectInfo` instead of pure `Object`s in order to track parameters of parameterized types and setters for fields. For more details see [Wrapper Classes](#wrapper-classes).
  1. When evaluating the expression, the result is also returned as a wrapper class. The reason is that the result can be used as a new context for parsing new expressions and might contain additional information. If you are just interested in the instance itself, then you can simply unwrap the result by calling `ObjectInfo.getObject()` as done in this example.   

# Features and Short Comings

Instead of listing all things that work as expected we will highlight positive and negative points that deviate from the expectations of a regular Java parser.

## Code Completion

If you only want to execute Java code (or at least something similar), then you can use [Groovy](http://www.groovy-lang.org/). It is much more powerful and probably also much more reliable. However, if you need code completion when writing a single expression, then Zenodot might be the right choice. Zenodot provides code completions for packages, classes, methods, and fields. The completions are rated according to a rating function that considers names and types. The rating algorithm supports camel case pattern matching, i.e., the completion "NullPointerException" gets a high rating if the search text is "NuPoEx". 

Note that the completions returned by the parsers are unsorted since it is the caller's responsibility to decide how to order them. However, the utility class `Parsers` provides a comparator `Parsers.COMPLETION_COMPARATOR` that is used in all Zenodot unit tests and that might yield acceptable results in several other use cases.

## Dynamic Typing

When inspecting the internal state of an object returned by a method, you sometimes have to cast it to its runtime type to be able to access its methods because they are not published via the declared type. To avoid such casts, you can activate dynamic typing. If this option is selected, then both, code completion and expression evaluation, ignore the declared type and use the runtime type of an object instead (see [Dynamic Typing Example](#dynamic-typing-example) for an example). Although this can be handy in some cases, you should be aware of the risks. If you call a method with side effects, then this side effect will also be triggered when requesting code completions or when evalutating an expression with a syntax error. Furthermore, method overloads can be resolved differently with static and dynamic typing.

## Custom Variables

Zenodot allows you to declare variables that can be set and accessed in expressions. This can save some typing when repeatedly evaluating expressions in a certain context.

## Custom Hierarchies

One of the most interesting features of Zenodot is its support of custom hierarchies. If an application holds a dynamically created tree, then this tree can, of course, be traversed with an arbitrary Java parser. However, in many cases the traversed nodes are only represented by generic node classes. Only in rare cases there will be one node class for each node. Consequently, when traversing such a tree, you will have to call generic methods like `getChild(childIndex)` or something similar. This is different than what you see in your application where every node is displayed with its individual name. When restricted to Java syntax, you can not hope for meaningful code completion here. You have to deal with child indices instead.

Zenodot extends the Java syntax to allow for parsing a custom hierarchy. The only thing the application developer has to do is to specify his tree in a form Zenodot understands by implementing a certain interface. A user can then traverse that tree using the node names.

**Example:** Assume that you have a document viewer application that has loaded this document. It might store the content in a hierarchy with sections on the first level, subsections on the second level, and so one. Let us assume that we want to evaluate the object behind this section, i.e., the section "Features and Short Comings" -> "Custom Hierarchies". A classic approach would be to call `getSection(4).getSubsection(3)`. As you can imagine, handling the indices, which is only a technical detail, will become troublesome in large trees. However, if you have configured Zenodot correctly, then you can also write `{Features and Short Comings#Custom Hierarchies}` to reference the same node in your tree. This is much more readable and less error-prone. (Also note the spaces inside the node names.) Furthermore, you can, e.g., request code completion after typing `{Features and Short Comings#Custom`. The result will be `Custom Variables` and `Custom Hierarchies`.

See [Custom Hierarchy Example](#custom-hierarchy-example) to see how to use custom hierarchies in Zenodot.

## Generics

The generic handling is currently based on the [Reflection API](https://github.com/google/guava/wiki/ReflectionExplained) of [Google Guava](https://github.com/google/guava). Due to type erasure, it is not possible to determine the parameters of parameterized types at runtime in general. However, it is possible to determine parameters when the declared type is known. If, e.g., it is known that an object is returned by a method that returns a `Collection<Integer>`, then the parameter `Integer` will be preserved. If the runtime type of the object is `ArrayList`, then Zenodot can even conclude that the actual type is `ArrayList<Integer>`. See [Tracking Parameters of Generic Types](#tracking-parameters-of-generic-types) for an example.

However, there are some things that Zenodot can currently not do:

  1. You can only cast objects to raw types, not parameterized types. A cast `(List)` is perfectly fine, but a cast `(List<Integer>)` will not work.
  1. Zenodot cannot deduce types: Without dynamic typing, the expression `java.util.Arrays.asList("a", "b", "c").get(0).length()` does not compile since Zenodot cannot infer that the created `List` is a `List` of `String`s.
  1. Zenodot allows calling a method that expects a `List` of `String`s with a `List` of `Integer`s. The reason is that, due to type erasure and the lack of type inference, Zenodot does not always have full generic type information. Since rejecting unresolved or only partially resolved types as method arguments will be wrong in some cases, we decided to ignore generic type parameters and only consider raw types when deciding whether a method may be called for a given list of arguments.  
  
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
ObjectInfo thisValue = InfoProvider.createObjectInfo("Zenodot");
ExpressionParser parser = Parsers.createExpressionParser(settings);
System.out.println("Result: " + parser.evaluate(expression, thisValue).getObject());
``` 

The expression "substring(4)" is evaluated using the string "Zenodot" as context. It is equivalent to the expression "this.substring(4)" and evaluates to "dot". 

# Handling Code Completions

Code completions are represented by the interface `CodeCompletion`. We will discuss some of its methods in detail:

  - `getInsertionRange()` returns the range of the current text that should be replaced by the code completion.
  - `getTextToInsert()` returns the text that should be used to replace the range of the current text specified by`getInsertionRange()`.  
  - `getCaretPositionAfterInsertion()` returns the position of the caret after inserting the code completion. In many cases it will be the end of the insertion range. For methods, however, it is the position after the opening parenthesis.
  - `toString()` returns the suggested text that should be displayed to the user. This is not always the same as the text returned by `getTextToInsert()`. For methods, for instance, it contains information about the argument types.

If the interface `CodeCompletion` does not provide sufficient information because you need to handle different types of code completions differently, then you can cast them to one of the following specific interfaces: `CodeCompletionClass`, `CodeCompletionPackage`, `CodeCompletionField`, `CodeCompletionMethod`, `CodeCompletionKeyword`, `CodeCompletionVariable`, or `CodeCompletionObjectTreeNode`.   

# Wrapper Classes

Zenodot uses wrapper classes for all kinds of relevant entities. These wrapper classes carry additional information and allow future extensions without breaking the API. The following wrapper classes are most relevant:

  - `TypeInfo`: A `TypeInfo` is comparable to Guava's `TypeToken` (currently it simply wraps it). Due to type erasure, parameters of generic types are lost at runtime. However, parameters of declared types are still available at runtime. The `TypeInfo` class tracks as much information as possible about generic type parameters in order to improve the quality of code completions.
  - `ObjectInfo`: An `ObjectInfo` does not only carry information about an object, but also about its declared type. From the declared type it is possible to derive parameters of generic runtime types to some extent. Additionally, an `ObjectInfo` carries information about where an object comes from. If it comes from a non-final field, it also carries a setter that allows modifying that field.
  - `ClassInfo`: A `ClassInfo` is more or less a plain String describing a class. Zenodot uses this wrapper class instead of a `Class<?>` instance in order to prevent loading classes unnecessarily. Whenever the real class object is needed, it suffices to call `Class.forName()` for the normalized name stored in a `ClassInfo`.
  - `PackageInfo`: A `PackageInfo` simply wraps a String describing a package. Call `Package.getPackage()` for the package name stored in that wrapper to obtain the corresponding `Package` object.

Whenever you need to create on of these wrapper classes, you have to use the utility class `InfoProvider`. Note that this class also contains some static constants. The constant `InfoProvider.NULL_LITERAL`, e.g., describes `null`.

## Tracking Parameters of Generic Types

**GenericParameterTrackingSample.java:** Consider the test class

```
static class TestClass
{
    public final List<String> list = Arrays.asList("This", "is", "a", "list", "of", "strings", ".");
}
```

and the following parser code:

```
TestClass testInstance = new TestClass();

ParserSettings settings = ParserSettingsBuilder.create().build();
ExpressionParser parser = Parsers.createExpressionParser(settings);
String text = "list.get(0).le";
ObjectInfo thisValue = InfoProvider.createObjectInfo(testInstance);
List<CodeCompletion> completions = new ArrayList<>(parser.getCompletions(text, text.length(), thisValue));
Collections.sort(completions, Parsers.COMPLETION_COMPARATOR);

System.out.println(completions.get(0).getTextToInsert());
```

An instance of `TestClass` is used as the context in which the expression "list.get(0).le" is parsed. Code completion is requested at the end of the expression. The completions are finally sorted according to Zenodot's default comparator for code completions and the best match is printed.

This code prints "length()" to the console. This shows that Zenodot is tracking parameters of generic runtime types: The runtime type of "list", which refers to the field `list` of the context `testInstance`, is `Arrays.ArrayList`. Its parameter class `String` is lost due to type erasure. However, Zenodot can deduce this parameter from the declared type `List<String>` of the field `list`. This is the reason why Zenodot can deduce that "list.get(0)" is a `String` and not only an `Object` and, hence, suggests the code completion "length()". Note that this works without enabling dynamic typing.

## Tracking Object Origins

**ObjectInfoSetterSample:** Consider the test class

```
static class TestClass
{
	public int test = 5;
}
```

and the following parser code:

```
TestClass testInstance = new TestClass();

ParserSettings settings = ParserSettingsBuilder.create().build();
ExpressionParser parser = Parsers.createExpressionParser(settings);

// First evaluation: Evaluate "this.test" for context testInstance
String firstExpression = "this.test";
ObjectInfo thisValue = InfoProvider.createObjectInfo(testInstance);
ObjectInfo referenceToField = parser.evaluate(firstExpression, thisValue);

// Second evaluation: Change value of testInstance.test to 7
String secondExpression = "this = 7";
parser.evaluate(secondExpression, referenceToField);

// Value of testInstance.test should now be 7
System.out.printf("Value of testInstance.test: " + testInstance.test);
```

In the first evaluation, Zenodot evaluates the expression "this.test", which refers to the field `test` of `testInstance`. At that time, the value of that field is 5. In the second evaluation, this `ObjectInfo` is used as context and this context is set to 7 by the expression "this = 7". This results in the field `test` of `testInstance` being set to 7. The reason for this is that an `ObjectInfo` does not only contain information about an object, but also about its origin. Hence, evaluating these two expressions has the same effect as evaluating the single expression "this.test = 7" when using `testInstance` as context.

# Parser Settings

It is obligatory to create an instance of `ParserSettings` in order to create a parser. This instance is created via a `ParserSettingsBuilder`, which is returned by the factory method `ParserSettingsBuilder.create()`. Several options are available:

  - Completion mode: With the completion mode you can configure how code completions are generated. You can specify whether the whole word or only the word until the caret is considered for suggesting completions and whether only the word until the caret or the whole word is proposed to be replaced. Example: Consider the text "text" and assume that the caret is between "te" and "xt". Furthermore, assume that there are two potential completions "test" and "texture".
    * `CompletionMode.COMPLETE_AND_REPLACE_UNTIL_CARET`: The text "te" is completed. Both completions, "test" and "texture", are matching completions of "te". The completions suggest to replace the text "te". The new text would therefore be "testxt" or "texturext", respectively.
    * `CompletionMode.COMPLETE_UNTIL_CARET_REPLACE_WHOLE_WORDS`: As in the previous mode, the text "te" is completed and, hence, both completions, "test" and "texture", match. However, the completions suggest to replace the whole word "text", leading to "test" or "texture", respectively.
    * `CompletionMode.COMPLETE_AND_REPLACE_WHOLE_WORDS` (default): The whole word "text" is completed. Here, only the completion "texture" matches the text "text". This completion suggests to replace the whole word "text" by "texture".    

  - Class imports: Imported classes can be referenced by their simple instead of their fully qualified names from within expressions.
  
  - Package imports: All classes in imported packages can be referenced by their simple instead of their fully qualified names from within expressions.
  
  - Variables: You can specify variables that can be referenced from within expressions. Use the overloads of `ParserSettingsUtils.createVariable()` to create variables. There you have to specify whether the values the variables point to should be referenced by a hard or by a weak reference. If the objects your variables point to are part of your application you want to debug, then in most cases it will be advisable to use weak references. Otherwise, you might extend the lifetime of these objects unintentionally.
  
  - Minimum access modifier: You can specify the minimum access modifier fields, methods, or constructors must have in order to be accessible by the parser. If you want to debug implementation details of objects, then you should set it to `AccessModifier.PRIVATE`. If you just want to call API methods, then it is advisable to set it to `AccessModifier.PUBLIC` (default). This may reduce the number of code completions significantly by filtering out irrelevant completions. Remarks:
    * For the sake of simplicity we decided that the accessibility of fields, methods, and constructors is independent of context the expression is evaluated in. If the minimum access modifier is `AccessModifier.PUBLIC`, then you cannot access protected fields, even if they are fields of the context.
    * The minimum access modifier is not considered when accessing classes. The reason is that we want to avoid loading classes only for determining their access modifier.

  - Dynamic typing: You can specify whether to enable or disable dynamic typing. By default, it is disabled. See [Dynamic Typing](#dynamic-typing) for details and [Dynamic Typing Example](#dynamic-typing-example) for an example.
  
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
    .enableDynamicTyping(true)
    .build();
String expression = "getObject().length()";
ObjectInfo thisValue = InfoProvider.createObjectInfo(testInstance);
ExpressionParser parser = Parsers.createExpressionParser(settings);
System.out.println("Result: " + parser.evaluate(expression, thisValue).getObject());
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
        public ObjectInfo getUserObject() {
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
List<CodeCompletion> completions = new ArrayList<>(parser.getCompletions(text, text.length(), InfoProvider.NULL_LITERAL));
Collections.sort(completions, Parsers.COMPLETION_COMPARATOR);

System.out.println("Completion: " + completions.get(0).getTextToInsert());

String expression = "{numbers#e}";
Object result = parser.evaluate(expression, InfoProvider.NULL_LITERAL).getObject();

System.out.println("Result: " + result);
```

The first part of the code requests Zenodot to suggest child nodes of the node for the path "strings" -> "long strings" considering the text "ve". The output will be "Completion: very long string". Note that node names can even contain white spaces. The only forbidden characters are '#' and '}'.

The second part of the code accesses the user object assigned to the node for the path "numbers" -> "e". The output is "Result: 2.72".

Observe that custom hierarchies are a language extension supported by Zenodot that allow semantic instead of syntactic code completions and expression evaluations. In contrast to class hierarchies that are determined at compile time (in most of the cases), custom hierarchies can be build and updated during an application's lifetime. 

# Open Source License Acknowledgement

Zenodot utilizes [Guava: Google Core Libraries for Java](https://github.com/google/guava). This library is licensed under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

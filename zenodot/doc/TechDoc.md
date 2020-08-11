# Technical Documentation

## TODO
- refactor TypeToken-Handling: Check that it can easily be exchanged by GenericX; benefit from parameterized objects by providing good suggestions, but do not suffer from overly strict argument checks resulting from unresolved type parameters (List<String> expected, but known type is only List<T>) or missing type inference support (Arrays.asList())
- support Arrays.asList()
- write PackageParser and ClassParser tests

## Preface

This documentation is meant for developers that want to know the core ideas behind the architecture of Zenodot. It is not suitable for API users.

## Expression Evaluation vs. Code Completions

Since providing code completions can only be done when the expression before the caret position has been parsed, there is only one algorithm for parsing expressions and providing code completions. If an expression has to be parsed, then the caret position is simply set to an invalid value (e.g., -1 or Integer.MAX_VALUE) to ensure that the algorithm does not encounter the caret when parsing the expression.

## Token

We call all semantic parts of an expression tokens. These can be field names and method names, but also characters like opening and closing parentheses or a dot. An instance of `TokenStream` provides methodes to query the next token. Whenever we read a token, we have to deal with the following cases:

- The token does not match our expectations (e.g., we expect an opening parenthesis after a method name, but find a different character).
- The token contains the caret, in which case a code completion has to be provided.

Since it is easy to forget to handle these cases when reading a token, all methods of `TokenStream` that read tokens force the caller to specify handlers for these cases.

We distinguish between character tokens (e.g., `(` and `.`) and  other tokens. The reason for this is that there are no code completions for character tokens. Hence, when the caret is at the end of a character token, then code completions are not generated when parsing the character token, but when parsing the subsequent token. However, when the caret is at the end of a non-character token, then code completions are generated when parsing that token. 

## Parsing Process

Internally, there exist several parsers. Each parser is responsible for parsing a subexpression under a certain assumption. For instance, the `ObjectFieldParser` parses a subexpression that starts with a field. Each of these parsers tries to parse the whole remaining expression starting from the current position, not only the first part that matches its expectations. If we are in the context of an object `x`, then the `ObjectFieldParser` interprets the subexpression `elements.size()` as method `size()` called on the field `elements` of object `x`. It does not stop parsing after parsing the subexpression `elements`. We refer to the part `.size()` in the aforementioned expression as *tail*. Each parser is also responsible for propagating parser errors and code completions, if requested.

Particularly in the context of code completions it is not clear which parser to use. When a code completion is requested after "xyz", it is not clear whether the user wants to refer to a field or to a method whose name starts with "xyz". This is why at any point each parser that might be applicable at that point is used to parse the remaining expression. The results of these parsers are then merged. Results can be:

- Code completions: All code completions have to be merged in order to be able to suggest fields and methods.
- Results: Whenever a result is encountered, it will be returned immediately. Further parsers are ignored. With this approach Zenodot realizes hiding: If there exists a variable with the same name as a field, then the variable is supposed to hide the field. Neither do we want the field to be preferred nor is this an ambiguous situation. By parsing the subexpression with the variable parser before parsing it with the field parser, it is guaranteed that a potential result of the variable parser prevents the field parser from being executed.
- Internal errors (`InternalErrorException`): Internal errors are propagated immediately.
- Evaluation exceptions (`EvaluationException`): Evaluation exceptions indicate that the parser that threw it was the correct one, but something went wrong when evaluating the expression. Hence, evaluation exceptions are propagated immediately.
- Syntax exceptions (`SyntaxException`): Syntax exceptions are thrown by parsers if the subexpression does not meet their syntactical expectations. If it is thrown by a parser that is sure to be the correct one, then the exception will be propagated immediately. Otherwise, it is collected and might be merged with syntax exceptions later.

### Merging Syntax Exceptions

Consider the two expressions "test" and "test()" and the variable parser (`VariableParser`), the field parser (`ObjectFieldParser`), and the method parser (`ObjectMethodParser`). Additionally, let us assume that neither a variable nor a field nor a method called "test" exists. In that case we expect an exception when parsing these expressions, but we are particularly interested in the exception message. In the case of "test" we would like to get a message like "unknown variable of field 'test'", whereas in the case "test()" we would like to get a message like "unknown method 'test'". Since all three parsers are considered and all of them throw the same exception in both cases, there must be some kind of mechanism to decide which exceptions to consider for the merge and which not. For this, the parsers' confidences (`ParserConfidence`) are considered.

When a parser is asked to parse a subexpression, then it is responsible for specifying how certain it is to be the correct parser for that subexpression at each point of its parsing process. There are three levels: At the beginning, each parser assumes to be the wrong parser (`ParserConfidence.WRONG_PARSER`). Later it might assess that it might be the correct parser (`ParserConfidence.POTENTIALLY_RIGHT_PARSER`). Finally, each parser should come to the conclusion that it is the correct one if this is true (`ParserConfidence.RIGHT_PARSER`).

The variable parser and the field parser only increase their confidence if there is no opening parenthesis behind the identifier. For the method parser, the opposite is the case. Consequently, for the expression "test", the variable parser and the field parser have a higher confidence than the method parser when they throw a parse exception. On the other hand, when parsing the expression "test()", the method parser has a higher confidence than the variable parser and the field parser when it throws a parse exception. Since the merging process only considers parse exceptions from parsers with the highest confidence, for the expression "test" the parse exception of the method parser is ignored, while for the expression "test()" the parse exceptions of the variable parser and the field parser are ignored.   

## Workflow Simplifications

It is quite cumbersome to check after each read of a token whether a syntax error was encountered or a code completion has been requested. These checks are necessary in order to decide whether to continue parsing or to propagate the error or the code completion. However, they distract from the real parsing code. This is why we decided to consider everything except successful parsing an exceptional behavior in the regular parsing process and throw exceptions to interrupt the parsing workflow. In particular, we also use exceptions to propagate code completions (`CodeCompletionException`). This seems a bit unnatural, but the parsing algorithm benefits in multiply ways from this approach:

- The parsing workflow is much clearer: The focus is on parsing. Everything else happens parallel to the regular workflow.
- The method signatures become much cleaner: The only thing a parse method can return is a result. Neither errors nor code completions have to be returned because they are propagated via exceptions.

## Parse Result Expectations

Each parser is given, among others, a parse result expectation (`ParseResultExpectation`) when parsing a subexpression. This describes the expectation of the caller. The expectation contains information about what type of result is expected - a package (`PackageParseResultExpectation`), a class (`ClassParseResultExpectation`), or an object (`ObjectParseResultExpectation`) - and whether the caller assumes that the parser has to parse the whole text in order to parse the subexpression (`AbstractParseResultExpectation.parseWholeText`). For objects, further expectations can be formulated: the expected type (`ObjectParseResultExpectation.expectedTypes`; required to rate code completions by their type) and whether the result has to match on of these types (`ObjectParseResultExpectation.resultTypeMustMatch`). All these expectations are automatically checked before a parser returns its result (`ParseResultExpectation.check()`). This helps detecting certain errors when they occur instead of only observing that the final result is wrong. Additionally, some parsers use the expectation to decide which parsers to use for parsing.

## Parameterized Parsers

Each parser has three generic parameters describing

- Which context they operate in,
- Which type the result will have, and
- Which type the parse result expectation has.

The context type can be package, class, or object:
- The package context, given as a `PackageInfo`, is used when parsing classes in a package specified earlier in the expression. For example, in "java.util.List" the package "java.util" is the context for the `QualifiedClassParser` that parses the subexpression "List".
- The class context, given as a `TypeInfo`, is used when parsing, e.g., fields or methods of a class specified earlier in the expression. For example, the class "java.lang.Math" in the expression "java.lang.Math.exp(2.5)" is the context for the `ClassMethodParser` that parses the subexpression "exp(2.5)".
- The object context, given as `ObjectInfo`, is used when parsing, e.g., fields or methods of an object specified earlier in the expression. For example, the instance returned by the subexpression "new int[]{ 0, 1, 2 }" in the expression "new int[]{ 0, 1, 2 }.length" is the context for the `ObjectFieldParser` that parses the subexpression "length".

The result of each parser is either

- an object (`ObjectParseResult`),
- a package (`PackageParseResult`), or
- a class (`ClassParseResult`).

Some parsers may return different types of results, depending on the expectation. For example, the `RootpackageParser` can be used to parse

- packages (returned in `AbstractPackageParser.doParse()` if the expression ends with a package name),
- qualified classes (returned via `AbstractPackageParser` -> `QualifiedClassParser` -> `ClassTailParser.doParse()` if the class is not prepended by '.' or '['), and,
- e.g., static fields of qualified classes (returned, e.g., via `AbstractPackageParser` -> `QualifiedClassParser` -> `ClassTailParser` -> `ClassFieldParser.parseNext()`).

The parser's result is (more or less) independent of the parse result expectation, but depending on the expectation this result is either returned (if it matches the expectation) or an exception (`SyntaxException`) is thrown.

The expected parse result is either

- an object (`ObjectParseResultExpectation`),
- a package (`PackageParseResultExpectation`), or
- a class (`ClassParseResultExpectation`).

Both parameters, the real result type and the expected result type, must be specified for each parser although they have to match. A parser that only returns objects only accepts object parse result expectations. This redundancy is technically inevitable and we need to know the concrete parse result expectation class because the classes provide different information. For example, the class `ObjectParseResultExpectation` contains, unlike the other parse result expectation classes, information about the expected type of the result. This information is used in order to rate code completions (`ObjectParseResultExpectation.rateTypeMatch()`).

## Compiled Expressions

When an expression is requested to be evaluated, it is internally compiled (not by a real compiler, but by a preprocessing step of Zenodot) and then the compiled expression is evaluated. This approach has two benefits:

- If the expression was evaluated immediately and during the evaluation a syntax error is encountered, then we might already have caused side effects. Consider, e.g., the expression "f(g(x), y)" and assume that no variable or field "y" exists. If the expression would have been evaluated when parsing the expression, then the method "g(x)" would have already been evaluated before detecting the problem that "y" is unknown. If the method "g" had any side effects on "x", then evaluating "g(x)" although the expression cannot be parsed would be unexpected behaviour.
- Parsing an expression over and over again for different values of `this`, like a lambda that is applied on each element on a stream, is very time-consuming since many of parsers have to be tested at any parsing stage. For these scenarios, Zenodot does not only offer to evaluate an expression, but it also offers the user the compiled expression which can be evaluated much more efficient for different values of `this`.    

### Dynamic Typing

Zenodot provided a feature called dynamic typing (`ParserSettings.isEnableDynamicTyping()`). With dynamic typing, runtime types rather than declared types are used for evaluations and, in particular, for resolving method overloads. Dynamic typing saves writing casts and improves the quality of code completions because these are then also based on the exact runtime type. With dynamic typing enabled, the expression is already evaluated during the parsing process, e.g., when code completions are requested. This is problematic if method calls are involved that cause side effects.

Note that even in the presence of dynamic typing a compiled expression is generated which can be used to evaluate the expression for other values of `this`. It is important to keep in mind that also the compilation process uses runtime types instead of declared types. When evaluating the compiled expression for different values of `this`, then you might get exceptions if these values have a different runtime type. Reasons for this are that the expression might access members that are specific to the runtime type used for the compilation or that the expression contains a call to a method with multiple overloads and the overload used when compiling the expression is not suitable for the other values used for `this`.
      
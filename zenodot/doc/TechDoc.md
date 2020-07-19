# Technical Documentation

## TODO
compilation:
- Object parse results should always be checked by the abstract parser whether they are compiled or not (against the compilation flag)
- Whether an expression should be compiled and whether a parser should check the expected result type and result class should also be described by the ParseExpectation
- Why do we distinguish between a "normal" ObjectParseResult and a "compiled" ParseResult? Maybe we can combine it?

- remove type token dependency and use class instead; However, make clear in the API that pure objects anfferent parsers in order to filter the most likely parser error when med classes are used and leave room for supporting generix later (e.g., method names should make clear which variant is used; no overloads with Object vs. ObjectX)
  

Since providing code completions can only be done when the expression before the caret position has been parsed, there is only one algorithm for parsing expressions and providing code completions. If an expression has to be parsed, then the caret position is simply set to an invalid value (e.g., -1 or Integer.MAX_VALUE) to ensure that the algorithm does not encounter the caret when parsing the expression.

## Token

We call all semantic parts of an expression a token. These can be field names and method names, but also characters like opening and closing parentheses or a dot. An instance of `TokenStream` provides methodes to query the next token. Whenever we read a token, we have to deal

- with the case that the token does not match our expectations (e.g., we expect an opening parenthesis after a method name, but find a different character) and
- with the case that the token contains the caret, in which case a code completion has to be provided.

Since it is easy to forget to handle these cases when reading a token, all methods of `TokenStream` that read tokens force the caller to specify handlers for these cases.

We distinguish between character tokens (e.g., `(` and `.`) and  other tokens. The reason for this is that there are no code completions for character tokens. Hence, when the caret is at the end of a character token, then code completions are not generated when parsing the character token, but when parsing the subsequent token. However, when the caret is at the end of a non-character token, then code completions are generated when parsing that token. 

## Parsing Process

Internally, there exist several parsers. Each parser is responsible for parsing a subexpression under a certain assumption. For instance, the `ObjectFieldParser` parses a subexpression that starts with a field. Each of these parsers tries to parse the whole subexpression, not only the first part that matches its expectations. If we are in the context of an object `x`, than the `ObjectFieldParser` interprets the subexpression `elements.size()` as method `size()` called on the field `elements` of object `x`. It does not stop parsing after parsing the subexpression `elements`. We refer to the part `.size()` in the aforementioned expression as *tail*. With that terminology, we can say that every parser first parses the first part of a subexpression under the assumption it has been designed for: The `ObjectFieldParser` expects the first part to be a field, the `ObjectMethodParser` expects the first part to be a method etc. Each parser is also responsible for propagating parser errors and code completions, if requested.

Particularly in the context of code completions it is not clear which parser to use. When a code completion is requested after "xyz", it is not clear whether the user is looking for a field or method whose name starts with "xyz". This is why at any point each parser that might be applicable at that point is used to parse the remaining expression. The result of these parsers is then merged:

- Code completions have the highest priority because if they exist, then the user must have requested them. Of course, all code completions have to be merged in order to be able to suggest fields and methods.
- Results have the second highest priority. Here we distinguish between unique and ambiguous results that may stem from an ambiguous method call.
- Finally, we consider parse errors. These are ordered according to their error priority and only those with the highest error priority priority will be considered for the final error. Example: If "xyz" is neither a field nor a method in the current context, then we will get parser errors from the `ObjectFieldParser` and the `ObjectMethodParser` when parsing "xyz" and both errors will be merged into the resulting error. If we parse "xyz(", then the error of the `ObjectMethodParser` gets a higher-rated error priority and, consequently, the error of the `ObjectFieldParser` will not be merged into the resulting error.

## Workflow Simplifications

It is quite cumbersome to check after each read of a token whether a parser error occured or a code completion has been requested. These checks are necessary in order to decide whether to continue parsing or to propagate the error or the code completion. However, they distract from the real parsing code. This is why we decided to consider everything except successful parsing an exceptional behavior in the regular parsing process and throw exceptions to interrupt the parsing workflow. In particular, we also use exceptions to propagate code completions. This seems to be a bit unnatural, but the parsing algorithm benefits in multiply ways from this approach:

- The parsing workflow is much clearer: The focus is on parsing. Everything else happens parallel to the regular workflow.
- The method signatures become much cleaner: The only thing a parse method can return is a result. Neither errors nor code completions have to be returned because they are propagated via exceptions.

## Compiled Expressions

For evaluating the same expression over and over again for different values of `this`, it is inefficient to always parse the expression. Hence, for this use case we support generating a "compiled" expression. Compiled expressions can be evaluated much faster than it takes to parse an expression. Since the base logic of parsing and compiling an expression is identical, the parsers can do both. This makes the code a bit less readable though and the return value of the parsing methods a bit more unspecific: It can either be a compiled result or a parse result. It would be nice if we could always simply compile the expression and then evaluate it, but then we would lose the dynamic typing feature for evaluated expressions: The parsing process with dynamic typing relies on evaluating partial expressions. Hence, parsing and evaluation cannot be separated here. 
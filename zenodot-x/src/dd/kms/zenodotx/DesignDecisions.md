# Design Decisions

The design of Zenodot X has been driven by the challenges faced during the development of Zenodot and Zenodot X. These are documented in the remainder of this document.

## Why Zenodot X?
Zenodot has been developed test-driven without knowing how far it would evolve. Over time, it became relatively powerful, but eventually it reached its limits:

* Parsing some operators like the conditional operator became hard to integrate into the existing architecture.
* It did not look like with the architecture at hand it would ever be possible to parse code blocks. However, without the capability of parsing code blocks, the planned feature "extension methods" only had half the benefit it could have.
* In Zenodot the responsibility for parsing partial expressions was given to individual parser implementations, roughly comparable to XML pull parsing. On the one hand, this allowed quite some flexibility for each parser. On the other hand, this had several disadvantages:
** It was hard to say how the syntax Zenodot is capable of parsing looks like by just looking at the code.
** Zenodot was hard to extend: For writing a new parser one had to understand all technical details.
** Since there was no actual parsing framework, no functionality could be reused for parsing other syntax than the Java syntax.

The goal of the project "Zenodot X" was to overcome the aforementioned drawbacks. It should provide a framework for parsing rules. The parsing should be the sole responsibility of the framework, where the rules, which should replace Zenodot's parsers, should only describe how to parse. Therefore, the framework calls methods of the individual rules, similar to XML push parsing where the parsing framework calls methods of the event handler. Most (if not all) of the syntax should be described at a central place. The framework could be reused for writing parsers for other languages. Additionally, the hope was that with an elaborate framework as solid foundation it should be easier to extend the supported language.

## Syntax vs. Semantic
The initial idea of Zenodot X was to create a syntax tree in a first step and then evaluate it. This idea failed in at least two aspects:

* One cannot tell apart packages, classes, fields, and variables by simply looking at the characters: `X.Y.Z` could have several meanings, among others:
** `X`, `Y`, and `Z` could be packages
** `X` could be a package, `Y` a top-level class, and `Z` a nested class
** `X` could be a variable and `Y` and `Z` fields
Parsing such an "expression" would either mean getting many different possible syntax trees that all have to be evaluated or having a syntax tree with nodes that could represent packages, classes, fields, and variables at once. Both approaches did not feel appealing.

* Code completions: Often no syntax tree can be built when code completions are requested because often the expression will not be syntactically correct in this case (consider, e.g., requesting code completion after writing `f(x`). With a strict separation of creating a syntax tree and then evaluating it, supporting code completions seemed to be extremely challenging.

Therefore, Zenodot X rules describe syntax and semantic. The framework tries to parse the syntax and, if successful, also to interpret it semantically. There are, e.g., distinct rules for packages, classes, fields, and variables. All of them have the same syntax, which accepts an identifier, but usually at most one will be able to parse the identifier semantically.

## Stack vs. Input-Output-Transformation
Zenodot X started with a stack-based evaluation approach: Rules could, whenever needed, push something onto a stack or read something from a stack. This allowed storing or retrieving evaluated data. While this allowed a relatively simple API, this had several problems:

* The code was not type-safe: Whatever had been popped from the stack had to be cast to the presumed type.
* The code was hard to read and error-prone: Each rule had to rely on its predecessors to keep the stack in a correct state. If one rule had corrupted the stack, then no error would have been detected unless popping the currently last element from the stack. Since rules could further push something onto the stack, this could theoretically have happened much later.

Therefore, Zenodot X uses an I-O-approach: Each rule gets an input (initially `null`) and produces and output. Some rules, e.g., plain character rules, simply pass the input to the successor rule without any transformation. In the expression `variable.field` the variable rule gets `null` as input and produces something representing the value of the variable. This value is given the character rule for `.` as input, which returns it again. Then the value is further passed to the field rule, which uses it to evaluate the field. As often with type-safety, this approach bloats the rule definitions syntactically because it forces implementers to define the input and the output type, which is not necessary with a stack-based approach.

## Two Kinds of Rules

It turned out that a single type of rule does not suffice to represent complex languages: Some rules are more for defining the flow of the parsing process, while others are responsible for parsing concrete parts of the expression. The former rules do not actually parse anything directly, but are composed of rules and know when to parse which, how to set the inputs of each of these rules, and what to do with their outputs. These rules are therefore called "compound rules". The latter are called "simple rules". The most relevant compound rules are the "or rule", which gets an input and successively tries to use it as input for the rules it contains until one succeeds, and the "then rule", which gets and input, uses it as input for its first rule, then uses the output of the first rule as input for the second rule, and then returns the output of the second rule.   

## Ambiguous Alternatives

Let's consider the syntax for a parameter list of a method or a constructor. It is

a. either empty
b. or a parameter, followed by an arbitrary number of a comma and a further parameter

When a non-empty parameter list is provided, then both alternatives match. The problem is that currently we have to decide now which path to take without having the information of subsequent rules, particularly the subsequent rule that expects a closing parenthesis `)`.

The problem can be solved in different ways:

1. Keep the framework and the ambiguity, but ensure that the preferred alternative is considered first. In this case we could first consider the non-empty parameter list and, if it does not match, then the empty parameter list, relying on the framework to stop at the first alternative that matches. This approach has the following rules:
** It is not clear whether this simple trick works in other cases as well.
** The solution does not work if there is a non-empty parameter list, but with a syntax error. In this case, the framework will next try to parse the empty parameter list and parse it successfully. The resulting error will be something like "')' is missing", which is incorrect. 
2. Keep the framework, but resolve the ambiguity: We could make the framework unambiguous by requiring that after the empty parameter list there is a closing parenthesis `)`. Note that we don't want to parse it (this is the responsibility of the subsequent rule), but we only want to look ahead. This approach requires extending the framework by some `peek` rule or similar. The drawback of this approach is that some rules must now know in which context they are applied, making it less likely that we can reuse it.
3. Rework the framework to handle ambiguity: This would probably be the cleanest approach, but we could not yet come up with a new concept that could handle this. The "or rule" could return multiple possible outputs, but it is not clear yet how to proceed with this. Additionally, a syntax error when parsing one alternative does not necessarily mean that this is the wrong path though another alternative could be parsed without error (cf. aforementioned example). Therefore, the caller of the "or rule" must be aware of the successful parse result, which it tries to parse further, and the syntax error. Then he can decide whether the syntax error or a potentially new syntax error has higher priority. The drawback of this approach is obviously that it requires a rework of the concept and that it is currently unclear whether this will work in the end.

The current favorite is the second approach, but the third approach is appealing because those how define a grammar for a language then don't have to think about ambiguity much.

## Yielding correct errors

As we have discussed before, first we parse the syntax of a partial expression, and then we evaluate it semantically. The original idea was that, when we have alternatives and none of them can be parsed successfully, then a semantic error is more reliable than a syntax error because it means that we have already parsed the syntax before. Hence, the idea was to return the semantic error instead of the syntax error for describing why the expression could not be parsed. It turned out that this approach does not work reliably. Consider, e.g., the following expression: `f(§`. Though it looks like a method call, we would get a semantic error that there is no field `f`. The reason is that, currently, the field rule only expects an identifier, which is `f`. The syntax for the field rule can be parsed, but then we get a semantic error that the field `f` is unknown. On the other hand, when applying the method rule, then we might either stop when evaluating `f` semantically because the method does not exist, or even worse, if it exists, then we get a syntax error when parsing `§`. This syntax error is the correct one, but the semantic error of the field has been preferred initially.

There are different approaches to handle this issue:

1. The field rule could be extended by expecting no `)` afterward (similar as approach 2 for resolving ambiguity). With this, we would get a syntax error rather than a semantic error, but still this alone would not completely resolve the issue in the case that we also get a syntax error when parsing the expression as method call.

2. When creating syntax and semantic errors, we should add an information how far we have parse the expression. We could then prefer paths for which we reached positions closer to the end of the expression. This alone would not completely resolve the issue in the case that `f` is no method.

3. When getting a semantic error, we could continue parsing at least the syntax of subsequent rules to see how far we can come in order to get a higher position that we can assign to the semantic error.

Approaches 1 and 2 together would solve the issue in our example when preferring semantic errors over syntactic errors if we reached the same position with both because for the field rule we would get a syntax error, but for the method rule we would either get a semantic error for the same position (which would then be preferred) or a syntax error at a later position, which would be preferred. However, having to add a not-`)` rule to the field rule feels about dirty.

Approaches 2 and 3 together would solve the issue in our example too: For the method rule we would either get a semantic error or a syntactic error, depending on whether there is a method `f`, but in any case the syntax can be parsed further. While approach 3 could be realized for some basic rules, we don't want implementers of all other compound rules carry such a burden. Hence, a feasible realization of approach 3 would be to implement it partially, which is also not very clean.

Note that the problem is related to the ambiguity problem mentioned one section before, so we will have to consider both when deciding for a solution. 

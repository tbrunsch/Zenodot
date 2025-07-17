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

## Handling Alternatives in the Or-Rule

### Ambiguous Alternatives

Let's consider the syntax for a parameter list of a method or a constructor. It is

a. either empty
b. or a parameter, followed by an arbitrary number of a comma and a further parameter

When a non-empty parameter list is provided, then both alternatives match. The problem is that, as of now, we have to decide which path to take without having the information of subsequent rules, particularly the subsequent rule that expects a closing parenthesis `)`.

The initial idea was that the "or rule" returns the result of the first matching rule (if any). Based on this, the problem can be solved in different ways:

1. Keep the framework and the ambiguity, but ensure that the preferred alternative is considered first. In this case we could first consider the non-empty parameter list and, if it does not match, then the empty parameter list, relying on the framework to stop at the first alternative that matches. This approach has the following drawbacks:
   ** It is not clear whether this simple trick works in other cases as well.
   ** The solution does not work if there is a non-empty parameter list, but with a syntax error. In this case, the framework will next try to parse the empty parameter list and parse it successfully. The resulting error will be something like "')' is missing", which is incorrect.

2. Keep the framework, but resolve the ambiguity: We could make the framework unambiguous by requiring that after the empty parameter list there is a closing parenthesis `)`. Note that we don't want to parse it (this is the responsibility of the subsequent rule), but we only want to look ahead. This approach requires extending the framework by some `peek` rule or similar. The drawback of this approach is that some rules must now know in which context they are applied, making it less likely that we can reuse it.
 
3. Rework the framework to handle ambiguity: This would probably be the cleanest approach, but we could not yet come up with a new concept that could handle this. The "or rule" could return multiple possible outputs, but it is not clear yet how to proceed with this. All other compound rules would have to deal with multiple results and produce multiple results themselves. Additionally, a syntax error when parsing one alternative does not necessarily mean that this is the wrong path though another alternative could be parsed without error (cf. aforementioned example). Therefore, the caller of the "or rule" must be aware of the successful parse result, which it tries to parse further, and the syntax error. Then he can decide whether the syntax error or a potentially new syntax error has higher priority. The drawback of this approach is obviously that it requires a rework of the concept and that it is currently unclear whether this will work in the end.

### Yielding correct errors

As we have discussed before, first we parse the syntax of a partial expression, and then we evaluate it semantically. The original idea was that, when we have alternatives and none of them can be parsed successfully, then a semantic error is more reliable than a syntax error because it means that we have already parsed the syntax successfully before. Hence, the idea was to return the semantic error instead of the syntax error for describing why the expression could not be parsed. It turned out that this approach does not work reliably. Consider, e.g., the following expression: `f(§`. Though it looks like a method call, we would get a semantic error that there is no field `f`. The reason is that, currently, the field rule only expects an identifier, which is `f`. The syntax for the field rule can be parsed, but then we get a semantic error that the field `f` is unknown. On the other hand, when applying the method rule, then we might either stop when evaluating `f` semantically because the method does not exist, or even worse, if it exists, then we get a syntax error when parsing `§`. This syntax error is the correct one, but the semantic error of the field has been preferred initially.

There are different approaches to handle this issue:

a. The field rule could be extended by expecting no `)` afterward (similar as Approach 2 for resolving ambiguity). With this, we would get a syntax error rather than a semantic error, but still this alone would not completely resolve the issue in the case that we also get a syntax error when parsing the expression as method call.

b. When creating syntax and semantic errors, we should add an information how far we have parse the expression. We could then prefer paths for which we reached positions closer to the end of the expression. This alone would not completely resolve the issue in the case that `f` is no method.

c. When getting a semantic error, we could continue parsing at least the syntax of subsequent rules to see how far we can come in order to get a higher position that we can assign to the semantic error.

Approaches a and b together would solve the issue in our example when preferring semantic errors over syntactic errors if we reached the same position with both because for the field rule we would get a syntax error, but for the method rule we would either get a semantic error for the same position (which would then be preferred) or a syntax error at a later position, which would be preferred. However, having to add a not-`)` rule to the field rule feels a bit dirty.

Approaches b and c together would solve the issue in our example too: For the method rule we would either get a semantic error or a syntactic error, depending on whether there is a method `f`, but in any case the syntax can be parsed further.

Note that the problem is related to the ambiguity problem mentioned one section before, so we will have to consider both when deciding for a solution.

### Current Solution

We solved the problem of yielding correct errors with Approaches b and c. Additionally, we changed the handling of results when parsing the "or rule" and applied the same logic as for determining the correct errors. This seemed to be a consistent approach. The current solution can be described as follows:

* The "or rule" does not return the first matching alternative (if any), but collects everything: results, syntax errors, semantic errors, and code completions. That way, the order of the alternatives does not matter anymore, which is a clear decision against Approach 1. With this, we have the chance to consider the parameter list alternative though the empty rule always matches.
* We determine how far we could parse an alternative purely syntactically. This does not affect the outcome of that alternative, but how it is weighted: The further we could parse an alternative, the more "likely" it is that it is chosen as the correct alternative. This is Approach c. Note that, when an alternative could be parsed successfully or when we get a syntax error, then we don't need to parse syntactically again, but we already know how far we could parse. It is only relevant when we obtain a semantic error to determine the correct error message.  
* The aggregation logic is now as follows:
  * When there are code completions, then these will be returned.
  * Otherwise, the outcome will be that of the alternative for which we could parse furthest (Approach b). Ties are (partially) broken as follows:
    * Results are favored over semantic errors, which are favored over syntactic errors.
    * If the outcome is still ambiguous, then we must merge the possible outcomes.

### Required Adaptions of this Approach

Consider the invalid expression "(byte)". There are (at least) two conflicting alternatives when trying to parse this expression:

1. The expression could be interpreted as field "byte" wrapped in parentheses. This interpretation would lead to a semantic error because "byte" is no field.
2. The expression could be interpreted as casting something to "byte". This interpretation would lead to a syntax error because the expression that shall be cast to byte is missing.

With our solution, after detecting that "byte" is no field, the "or rule" will nevertheless parse the expression syntactically until reaching the end of the expression. Hence, both interpretations can be parsed syntactically to the end. Since the first interpretation "only" yields a semantic error and no syntactic error like the second interpretation, the former one would be preferred, which is not what we want. The problem is that our current solution does not consider how far both expressions could be parsed semantically.

As a workaround we still compare the maximum positions until which the expression can be parsed syntactically, but if both are the same, then we compare how far the expression could be parsed semantically with these interpretations. This solves the problem in our example because for the first interpretation the semantic error already occurs at "byte", whereas for the second interpretation no semantic error occurs at all.

Only if two alternatives have identical semantic and syntactic parse positions, then we break ties as described in the previous section: Results are favored over semantic errors, which are favored over syntactic errors. 

## Managing Parser States
In some cases it is important that compound rules can influence the parser's state:

* The "or rule" tests several alternatives and eventually decides which was the most likely one. For this, the following operations are required:
  * Storing and restoring the parser state: Restoring the initial state must be done before testing any of the alternatives.
  * Getting and setting the parser state: Setting the state is required when deciding for the most likely alternative. In that case, the state after parsing that alternative must be restored.
* The "repetition rule" greedily tries to apply a rule as many times as possible. For this, the following operations are required:
  * Storing and restoring the parser state: When the rule detects that is has been trying to repeat the rule once too often, then it must restore the state before applying the last repetition. That state must have been stored before.
  * Dropping a parser state: The state must be stored before trying the next repetition. However, when a repetition succeeds, then the stored state must be dropped (not applied).

### Problems with Using a Stack for managing the stored States

As the terms "store" and "restore" may suggest, the initial idea was to manage the stored states in a stack. Restoring a state meant to remove the top-most state from that stack and to apply it. The motivation was that parser states should not be handed over to rules. However, several reasons led to a reconsideration:

* Initially, the "or rule" was greedy: It chose the first matching alternative. At that time it was not necessary to get or set a parser state: Either an alternative was considered ok, in which case the current state remained unchanged, or not, in which case the initial state had been restored. With the new solution to consider all alternatives and eventually decide for the best one, getting and setting parser states became necessary because this could not be resembled by storing and restoring states. Hence, it became necessary to hand parser states over to compound rules.
* The stack of parser states is yet another point of failure via which faulty rules could influence other rules negatively: If a rule forgets to drop a state it has stored earlier, then everything works fine as long as the next rule tries to restore its state, which would then restore the other rule's state unintentionally. That way, the bug in one rule would manifest as incorrect behavior of another rule, contradicting a desired fail-fast strategy.
* Working with a generic stack is much less expressive than using getters and setters because there you could use meaningful names for the stored states:

```
parser.storeState();
// ...
parser.restoreState();
```

vs.

```
ParserState initialState = parser.getState();
// ...
parser.setState(initialState);
```

* Restoring the initial state in an "or rule" everytime before trying an alternative is much easier to achieve with setters and getters compared to storing and restoring states. Additionally, it obsoletes the necessity for dropping states in the "repetition rule".  

### New Approach for handling Parser States

One could try to rescue the parser-state-stack-approach by checking that no compound rule may drop or restore a rule it hasn't stored before. Additionally, when a compound rule finishes parsing, one could automatically remove all states from the stack the rule had pushed onto it. This approach still has several flaws:

* It checks integrity at runtime instead of preventing inconsistency in the first place.
* This approach does not address the other issues mentioned above: Setters and getters remain necessary in addition to store, restore, and drop operations.

As a result, we decided against a stack-based approach in favor of a pur getter-setter approach.

## Storing the successfully parsed simple Rules

It turned out that debugging the parsing framework of Zenodot X is even more complex than debugging the parsing framework of Zenodot: We have tried to provide reusable compound rules like the "or rule", the "then rule", and the "repetition rule". This partially eliminates the need for writing custom compound rules for parsing structures as it was the case in Zenodot. The drawback of this is that when stopping at a breakpoint it is much harder to see where we currently are in the parsing process because one will often be in the parser or in one of the aforementioned compound rules. To improve here, the parser now holds a collection of the simple rules parsed so far and which part of the expression each covers. Since this information is only required for debugging purposes, it is important that keeping this information up-to-date must not cost much time. To achieve this, an appropriate data structure is required, and therefore we need to understand the operations the data structure needs to support:

* Appending an element to the data structure must be efficient: Whenever we have successfully parsed a new simple rule, it will be appended to that data structure.
* Creating a copy of the data structure must be efficient: Since the collection of parsed simple rules is part of the parsers state and since we must be able to get and set the state, it must be efficient to copy that data structure. A state must not only keep a reference to the data structure since the original data structure is going to change. 

Under the assumption that the elements of the data structure are not changed, which is valid in our case and which would otherwise force creating copies of each element, it is possible to come up with a data structure that allows appending and even copying in constant time. The solution is a reverse linked list, i.e., a list in which the elements are linked from tail to head. Despite the term "linked list" it can actually serve as a stack:

* The stack wraps all its elements in a stack element instance, which contains the element and has a link to the previous stack element.
* The stack itself only holds a reference to the last element in the stack (the top-most).
* When pushing an element onto the stack, we wrap it in a new stack element instance, which points to the previously last element, and make it that the last element of the stack.
* When popping an element (not required for our use cases), we move to the second to the last element and consider that one the new last element of the stack.
* For creating a copy of the stack, we create a new stack instance that simply references the last element of the original copy. Both stacks now share the same wrapped elements, which is why the copy could be created in constant time.

Note that stack operations on both stacks, the original one and the copy, don't interfer with each other:
* By assumption, the elements itself don't get modified.
* Independent of what the stacks do, the stack elements remain linked the way they are:
  * Pushing an element to one of the stacks only adds a new stack element, which references an existing one. The links of the existing ones remain. The only thing that changes is that the modified stack now references another stack element. This does not affect the other stack.
  * Popping an element from one of the stacks does not change existing links either. The modified stack now only references the previous stack element.

Note that over their life times such stacks create trees of stack elements. At any time, the stack represents a path from some tree node to the root. By copying the stack, one can represent multiple paths in this tree.

### Drawback of the Stack of parsed Simple Rules

We have introduced a collection of parsed simple rules to help orienting within the parsing process when debugging. The selected data structure allows this with constant overhead per operation. However, IDEs understandably don't have a helpful representation of this data structure: One must move from stack element to stack element, starting at the last one, and step into the stack elements to see the actual elements. This is unacceptable for debugging.

Therefore, we decided to add a read-only `List` view to the stack element data structure by letting it extend that interface. This caused the next problems: Accessing the first element already costs linear time because we start at the last stack element and then move to the first one by passing all other stack elements once. Iterating the whole list once (from front to back) would cast quadratic time, which is again unacceptable. Therefore, we decided to create and cache the whole list view at the first read access. This implies that independent of whether one element or all elements are accessed, the total access time is linear. For a random access `List` implementation this a linear access time for one element is not ok, but during debugging the IDE will access many (or all) elements of the data structure anyway, so the access time is ok in that case. 

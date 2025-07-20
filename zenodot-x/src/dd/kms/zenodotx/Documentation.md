# Rules

`Rule`s take an input of type `I` (and settings of type `S` that can control the parsing process) and produce an output of type `O`. The outmost rule is often a rule that does not have any input (type `Void`) and produce the desired output. Rules are often meant to be executed one after another (a sequence): The output of one rule is then the input for the next rule. Details will become clear when considering a concrete rule set (see section [Java Rules](#java-rules)).

## SimpleRules, CompoundRules, and EvaluationRules:
There are essentially three types of `Rule`s: `SimpleRule`s, `CompoundRule`s, and `EvaluationRule`s.

* `CompoundRule`s have access to the `Parser` that they can use to parse rules they are composed of (not themselves, because this would cause a stack overflow). `CompoundRule`s can therefore control the parsing process and decide when to parse which rule and how. It is possible to implement custom `CompoundRule`s, but the hope is that many use cases can be covered by implementing `SimpleRule`s or using predefined `CompoundRule`s. The predefined `CompoundRule`s are as follows:

  * `OrRule`: An `OrRule` that takes an input of type `I` and produces and output of type `O` contains several rules that also type an input of type `I` and produce an output of type `O`. The first of these rules that can be parsed successfully "wins". When a rule handles an event (see Section [Events](#events)), then the `OrRule` lets also the alternatives handle the event and merges the results of these handling processes and propagates the result. Use one of the overloads of `Rules.or()` to create an `OrRule`.

  * `RepetitionRule`: A `RepetitionRule` is a special rule where the input type `I` and the output type `O` are identical (called `IO` in the remainder). It wraps a rule that also has input and output type `IO` and represents a repetition of that rule of variable length (any non-negative integer, including 0). When parsing the `RepetitionRule`, the maximum number of possible repetitions will be chosen. If an event is encountered when parsing the wrapped rule, then the result of the event handling will be propagated by the `RepetitionRule`. Use one of the overloads of `Rules.repeat()` to create a `RepetitionRule`.

  * `ThenRule`: The `ThenRule` describes a sequence of two rules: The first rule takes an input of type `I` and produces an output of type `O`. The second rule takes this output of type `O` and produces an output of type `T`. Consequently, the resulting `ThenRule` has input type `I` and output type `T`. It propagates the result of a potential event handling. To create a `ThenRule` of `rule1` and `rule2`, call `rule1.then(rule2)`. 

  * `DelegatingRule`: This rule wraps another `CompoundRule` and delegates to it. This rule can be used when rules have to be defined recursively (see Section [Recursive Expressions](#recursive-expressions)).

* `SimpleRule`s describe their syntax (`SyntaxRule`) and ... TODO


TODO: predefined `SimpleRule`s: ...

* `EvaluationRule`s ... TODO

### Recursive Expressions

Sometimes, expressions are defined recursively, referencing themselves. This is not trivial to realize. One can achieve this by declaring a mutable rule and initializing it somehow. Afterward, one can define it recursively because in this definition the rule can now be referenced. Rules for which such a workaround can be applied are `OrRule`s, which should be used when the expression consists if different alternatives anyway, and `DelegatingRule`s, which are meant to use if there are no alternatives.

## Events

...

## Writing Rules

extend `AbstractRule`

...


## Parsing an Expression with Rules

...

# Java Rules

Concepts:
* static, dynamic, and mixed typing
* evaluate + compile (`InstanceParseResult`)
* ...

## InstanceParseResult

The `InstanceParseResult` covers two related functions:

* It contains information about the currently parsed/evaluated instance in form of an `ObjectInfo` (cf. `InstanceParseResult.getEvaluatedResult()`). The return value contains at least the class of the parse result and in some cases (for dynamic typing always, for mixed typing only if possible without side effects) the evaluated instance.

* It contains a method via which one can reproduce the evaluated result (cf. `InstanceParseResult.evaluate(JavaSettings)`). However, it is never called to reproduce exactly this result. Instead, the settings are slightly modified when evaluating the expression again as demonstrated by the following workflows where this method is called:

  * Actual expression evaluation: As mentioned in the previous point, the evaluated result returned by `InstanceParseResult.getEvaluatedResult()` does not always contain the evaluated instance. For static typing it usually only contains type information, and for mixed typing it only contains the evaluated instance if the evaluation did not cause any side effects. To obtain the evaluated object, the method `InstanceParseResult.evaluate(JavaSettings)` is called with almost the same settings, but with a mode that forces the evaluation of all subexpressions.

  * Compiling expressions: When parsing, e.g., a lambda expression, then this expression is parsed without actually evaluating something. However, the returned `InstanceParseResult` contains information how the lambda expression has to be evaluated depending on the lambda parameters, which are given as variables by the `JavaSettings`. To evaluate the lambda for certain parameter values, the corresponding variables will be assigned these parameter values and then `InstanceParseResult.evaluate(JavaSettings)` is called with these settings and a mode that forces the evaluation of all subexpressions.

### Implementing InstanceParseResult

We have already seen that the two methods of `InstanceParseResult` are related to each other, so it makes sense to extract common functionality into a method when implementing `InstanceParseResult`. On a very abstract level this can look as follows:

Typically one does not handle the whole expression at once, but just one small parsing step (e.g. parsing a field) that is evaluated in a certain context (the instance for which the field shall be evaluated).

* Extract the evaluation of this single step into a method. For the sake of brevity let this function be named `f`.
* The method `getEvaluatedResult()` should return an `ObjectInfo` that one obtains by applying `f` on the evaluated result(s) of the context. Note that we don't evaluate anything except for `f` here, but work with what has already been evaluated. This is not only important for performance reasons, but also because for dynamic typing we could cause side effects multiple times, which is not intended.
* The method `InstanceParseResult.evaluate(JavaSettings)`, on the other hand, returns an `ObjectInfo` that one obtains by first evaluating the context and then applying `f` on the evaluated result(s) of the context. Note that the evaluation of the context will itself trigger further evaluations recursively. In the end, the whole expression will be evaluated.

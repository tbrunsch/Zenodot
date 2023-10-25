# Custom Hierarchy Parser
This module is intended to extend the Zenodot basic parser by another parser that allows parsing custom hierarchies. We will elaborate on this in the remainder of this document.

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
# Table of Contents

- [Custom Hierarchies](#custom-hierarchies)
- [Custom Hierarchy Example](#custom-hierarchy-example)
- [Customizing the Custom Hierarchy Parser](#customizing-the-custom-hierarchy-parser)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# Custom Hierarchies

If an application holds a dynamically created tree, then this tree can, of course, be traversed with an arbitrary Java parser. However, in many cases the traversed nodes are only represented by generic node classes. Only in rare cases there will be one node class for each node. Consequently, when traversing such a tree, you will have to call generic methods like `getChild(childIndex)` or something similar. This is different from what you see in your application where every node is displayed with its individual name. When restricted to Java syntax, you cannot hope for meaningful code completion here. You have to deal with child indices instead.

This module extends the Java-like syntax supported by the basic Zenodot parser to allow for parsing a custom hierarchy. The only thing the application developer has to do is to specify his tree in a form the custom hierarchy parser understands by implementing a certain interface. A user can then traverse that tree using the node names.

**Example:** Assume that you have a document viewer application that has loaded a document with sections and subsections. Let us further assume that the 4th section is called "Features" and the 3rd subsection of this section is called "Extensibility". The document viewer might store the content in a hierarchy with sections on the first level and subsections on the second level. Let us assume that we want to evaluate the object behind the aforementioned subsection, i.e., "Features" -> "Extensibility". The classic approach would be to call `getSection(4).getSubsection(3)` on the document. As you can imagine, handling the indices, which is only a technical detail, will become troublesome in large trees. If you have configured the custom hierarchy parser correctly, then in Zenodot you could also write `{Features#Extensibility}` to reference the same node in your tree. This is much more readable and less error-prone. Furthermore, you can, e.g., request code completion after typing `{Features#Ex`. Among the code completions there will be `Extensibility`.

# Custom Hierarchy Example

In this section we discuss how to configure the parser for parsing such a hierarchy. For this, we consider *CustomHierarchySample.java* from the corresponding sample module. Assume that we have the hierarchy

  - numbers
    - pi (value = 3.14)
    - e (value = 2.72)
  - strings
    - short strings
      - test (value = "Test")
    - long strings
      - long string (value = "This is a long string.")
      - very long string (value = "This is a very long string.")

All leaves in this example have user objects (the "xxx" in "value = xxx"), but it is not uncommon that also inner nodes carry user objects. In order to make this hierarchy accessible for Zenodot, we have to represent it by a hierarchy of `ObjectTreeNode`s. Here we use the simple utility methods

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

Now consider the following parser code:

```
AdditionalParserSettings additionalParserSettings = CustomHierarchyParsers.createCustomHierarchyParserSettings(root);
ParserSettings settings = ParserSettingsBuilder.create()
	.additionalParserSettings(additionalParserSettings)
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

# Customizing the Custom Hierarchy Parser

By default, the custom hierarchy syntax uses the characters

* `{` to mark the beginning of a hierarchy expression,
* `#` as separator between hierarchy levels, and
* `}` to mark the end of a hierarchy expression.

However, by calling `CustomHierarchyParsers.createCustomHierarchyParserSettings(ObjectTreeNode, char, char, char)` you can define other characters. Note, however, that you must be sure that the syntax extension does not conflict with Java syntax nor with other syntax extensions.
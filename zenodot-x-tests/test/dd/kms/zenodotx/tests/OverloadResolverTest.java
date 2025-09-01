package dd.kms.zenodotx.tests;

import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodotx.java.overloadresolution.OverloadResolver;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OverloadResolverTest
{
	@Test
	public void testOverloadResolver() {
		OverloadResolver<String> resolver;

		// foo(int, int) vs. foo(int, long) called with foo(1, 2)
		resolver = new OverloadResolver<String>(int.class, int.class)
			.registerRegularOverload("Exact match", int.class, int.class)
			.registerRegularOverload("Widening match", int.class, long.class);
		testOverloadResolver(resolver, "Exact match");

		// foo(Object) vs. foo(int) called with foo((Integer) 1)
		resolver = new OverloadResolver<String>(Integer.class)
			.registerRegularOverload("Inheritance", Object.class)
			.registerRegularOverload("Unboxing", int.class);
		testOverloadResolver(resolver, "Inheritance");

		// foo(int) vs. foo(long) called with foo((short) 1)
		resolver = new OverloadResolver<String>(short.class)
			.registerRegularOverload("Widening to int", int.class)
			.registerRegularOverload("Widening to long", long.class);
		testOverloadResolver(resolver, "Widening to int");

		// foo(double) vs. foo(int) called with foo((short) 1)
		resolver = new OverloadResolver<String>(short.class)
			.registerRegularOverload("Widening to double", double.class)
			.registerRegularOverload("Widening to int", int.class);
		testOverloadResolver(resolver, "Widening to int");

		// foo(double) vs. foo(Integer) called with foo(1)
		resolver = new OverloadResolver<String>(int.class)
			.registerRegularOverload("Widening", double.class)
			.registerRegularOverload("Boxing", Integer.class);
		testOverloadResolver(resolver, "Widening");

		// foo(int, int) vs. foo(int...) called with foo(1, 2)
		resolver = new OverloadResolver<String>(int.class, int.class)
			.registerRegularOverload("Exact match", int.class, int.class)
			.registerVariadicOverload("Variadic", int[].class);
		testOverloadResolver(resolver, "Exact match");

		// foo(Object) vs. foo(String...) called with foo("bar")
		resolver = new OverloadResolver<String>(String.class)
			.registerRegularOverload("Inheritance", Object.class)
			.registerVariadicOverload("Variadic", String[].class);
		testOverloadResolver(resolver, "Inheritance");

		// foo(double) vs. foo(int...) called with foo(1)
		resolver = new OverloadResolver<String>(int.class)
			.registerRegularOverload("Widening", double.class)
			.registerVariadicOverload("Variadic", int[].class);
		testOverloadResolver(resolver, "Widening");

		// foo(Integer) vs. foo(int...) called with foo(1)
		resolver = new OverloadResolver<String>(int.class)
			.registerRegularOverload("Boxing", Integer.class)
			.registerVariadicOverload("Variadic", int[].class);
		testOverloadResolver(resolver, "Boxing");

		// foo(Object) vs. foo(String) called with foo(null)
		resolver = new OverloadResolver<String>(InfoProvider.NO_TYPE)	// used for representing "class" of null
			.registerRegularOverload("Object", Object.class)
			.registerRegularOverload("String", String.class);
		testOverloadResolver(resolver, "String");

		// foo(Object) vs. foo(Object...) called with foo(null)
		resolver = new OverloadResolver<String>(InfoProvider.NO_TYPE)	// used for representing "class" of null
			.registerRegularOverload("Object", Object.class)
			.registerVariadicOverload("Variadic", Object[].class);
		testOverloadResolver(resolver, "Variadic");

		// foo(String) vs. foo(String...) called with foo(null)
		resolver = new OverloadResolver<String>(InfoProvider.NO_TYPE)	// used for representing "class" of null
			.registerRegularOverload("String", String.class)
			.registerVariadicOverload("Variadic String", String[].class);
		testOverloadResolver(resolver, "String", "Variadic String");

		// foo(int[]...) vs. foo(int...) called with foo(new int[] { 1, 2 })
		resolver = new OverloadResolver<String>(int[].class)
			.registerVariadicOverload("Variadic Component Type Match", int[][].class)
			.registerVariadicOverload("Variadic Array Type Match", int[].class);
		testOverloadResolver(resolver, "Variadic Array Type Match");

		// foo(int, int...) vs. foo(int...) called with foo()
		resolver = new OverloadResolver<String>()
			.registerVariadicOverload("Int + Variadic Int", int.class, int[].class)
			.registerVariadicOverload("Variadic Int", int[].class);
		testOverloadResolver(resolver, "Variadic Int");

		// foo(int, int...) vs. foo(int...) called with foo(1)
		resolver = new OverloadResolver<String>(int.class)
			.registerVariadicOverload("Int + Variadic Int", int.class, int[].class)
			.registerVariadicOverload("Variadic Int", int[].class);
		testOverloadResolver(resolver, "Int + Variadic Int", "Variadic Int");

		// foo(int, int...) vs. foo(int...) called with foo(1, 2)
		resolver = new OverloadResolver<String>(int.class, int.class)
			.registerVariadicOverload("Int + Variadic Int", int.class, int[].class)
			.registerVariadicOverload("Variadic Int", int[].class);
		testOverloadResolver(resolver, "Int + Variadic Int", "Variadic Int");

		// foo(int, int...) vs. foo(int...) called with foo(1, 2, 3)
		resolver = new OverloadResolver<String>(int.class, int.class, int.class)
			.registerVariadicOverload("Int + Variadic Int", int.class, int[].class)
			.registerVariadicOverload("Variadic Int", int[].class);
		testOverloadResolver(resolver, "Int + Variadic Int", "Variadic Int");

		// foo(int, Object...) vs. foo(Object...) called with foo()
		resolver = new OverloadResolver<String>()
			.registerVariadicOverload("Int + Variadic Object", int.class, Object[].class)
			.registerVariadicOverload("Variadic Object", Object[].class);
		testOverloadResolver(resolver, "Variadic Object");

		// foo(int, Object...) vs. foo(Object...) called with foo(1)
		resolver = new OverloadResolver<String>(int.class)
			.registerVariadicOverload("Int + Variadic Object", int.class, Object[].class)
			.registerVariadicOverload("Variadic Object", Object[].class);
		testOverloadResolver(resolver, "Int + Variadic Object");

		// foo(int, Object...) vs. foo(Object...) called with foo(1, 2)
		resolver = new OverloadResolver<String>(int.class, int.class)
			.registerVariadicOverload("Int + Variadic Object", int.class, Object[].class)
			.registerVariadicOverload("Variadic Object", Object[].class);
		testOverloadResolver(resolver, "Int + Variadic Object");

		// foo(int, Object...) vs. foo(Object...) called with foo(1, 2, 3)
		resolver = new OverloadResolver<String>(int.class, int.class, int.class)
			.registerVariadicOverload("Int + Variadic Object", int.class, Object[].class)
			.registerVariadicOverload("Variadic Object", Object[].class);
		testOverloadResolver(resolver, "Int + Variadic Object");

		// foo(int, Object) vs. foo(Integer, int) called with foo(1, 2)
		resolver = new OverloadResolver<String>(int.class, int.class)
			.registerRegularOverload("Better in first argument", int.class, Object.class)
			.registerRegularOverload("Better in second argument", Integer.class, int.class);
		testOverloadResolver(resolver, "Better in first argument", "Better in second argument");

		// foo(long...) vs. foo(double...) called with foo(1)
		resolver = new OverloadResolver<String>(int.class)
			.registerVariadicOverload("Variadic longs", long[].class)
			.registerVariadicOverload("Variadic doubles", double[].class);
		testOverloadResolver(resolver, "Variadic longs");

		// foo(Integer...) vs. foo(Long...) called with foo(1)
		resolver = new OverloadResolver<String>(int.class)
			.registerVariadicOverload("Variadic Integers", Integer[].class)
			.registerVariadicOverload("Variadic Longs", Long[].class);
		testOverloadResolver(resolver, "Variadic Integers");

		// foo(Integer...) vs. foo(Long...) called with foo(1)
		resolver = new OverloadResolver<String>(int.class)
			.registerVariadicOverload("Variadic Integers", Integer[].class)
			.registerVariadicOverload("Variadic Longs", Long[].class);
		testOverloadResolver(resolver, "Variadic Integers");

		// foo(Long) vs. foo(Double) called with foo(1)
		resolver = new OverloadResolver<String>(int.class)
			.registerRegularOverload("Long", Long.class)
			.registerRegularOverload("Double", Double.class);
		testOverloadResolver(resolver);

		// foo(Long...) vs. foo(Double...) called with foo(1)
		resolver = new OverloadResolver<String>(int.class)
			.registerVariadicOverload("Variadic Longs", Long[].class)
			.registerVariadicOverload("Variadic Doubles", Double[].class);
		testOverloadResolver(resolver);
	}

	private void testOverloadResolver(OverloadResolver<String> resolver, String... expectedOverloadsArray) {
		Set<String> expectedOverloads = new HashSet<>();
		expectedOverloads.addAll(Arrays.asList(expectedOverloadsArray));
		List<String> actualOverloads = resolver.getBestMatchingOverloads();
		for (String actualOverload : actualOverloads) {
			assertTrue("Unexpected overload: " + actualOverload, expectedOverloads.remove(actualOverload));
		}
		for (String expectedOverload : expectedOverloads) {
			fail("Missing overload " + expectedOverload);
		}
	}
}

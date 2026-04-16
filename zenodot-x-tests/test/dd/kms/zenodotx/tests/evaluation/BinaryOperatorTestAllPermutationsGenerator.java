package dd.kms.zenodotx.tests.evaluation;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class generates the test {@link BinaryOperatorTestAllPermutations} and writes it to the console.
 */
public class BinaryOperatorTestAllPermutationsGenerator
{
	private static final String	TEST_CODE_TEMPLATE_FILE_NAME	= "BinaryOperatorTestAllPermutationsTemplate.java";
	private static final String	TEST_CLASS_NAME					= "BinaryOperatorTestAllPermutations";
	private static final String	TEST_BUILDER_NAME				= "testBuilder";
	private static final String	TEST_INSTANCE_CLASS_NAME		= "TestClass";
	private static final String	TEST_INSTANCE_NAME				= "testInstance";

	public static void main(String[] args) throws IOException {
		List<String> testCollectionMethodCallLines = new ArrayList<>();
		List<String> testCollectionMethodLines = new ArrayList<>();

		addOperator12Tests(testCollectionMethodCallLines, testCollectionMethodLines);
		addOperator11Tests(testCollectionMethodCallLines, testCollectionMethodLines);
		addOperator10Tests(testCollectionMethodCallLines, testCollectionMethodLines);
		addOperator9Tests(testCollectionMethodCallLines, testCollectionMethodLines);
		addOperator8Tests(testCollectionMethodCallLines, testCollectionMethodLines);
		addOperator7Tests(testCollectionMethodCallLines, testCollectionMethodLines);
		addOperator6Tests(testCollectionMethodCallLines, testCollectionMethodLines);
		addOperator5Tests(testCollectionMethodCallLines, testCollectionMethodLines);
		addOperator4Tests(testCollectionMethodCallLines, testCollectionMethodLines);
		addOperator3Tests(testCollectionMethodCallLines, testCollectionMethodLines);
		addOperator1Tests(testCollectionMethodCallLines, testCollectionMethodLines);

		String successfulTests = String.join("\r\n", testCollectionMethodCallLines);
		String testsWithError = String.join("\r\n", testCollectionMethodLines);

		String testCode = createTestCode(successfulTests, testsWithError);

		System.out.println(testCode);
	}

	private static void addOperator12Tests(List<String> testCollectionMethodCallLines, List<String> testCollectionMethodLines) {
		createTestCollectionMethod(
			12,
			testCollectionMethodCallLines,
			testCollectionMethodLines,
			(successfulTestLines, testWithErrorLines) -> addNumericOperatorTests("*", successfulTestLines, testWithErrorLines),
			(successfulTestLines, testWithErrorLines) -> addNumericOperatorTests("/", successfulTestLines, testWithErrorLines),
			(successfulTestLines, testWithErrorLines) -> addNumericOperatorTests("%", successfulTestLines, testWithErrorLines)
		);
	}

	private static void addOperator11Tests(List<String> testCollectionMethodCallLines, List<String> testCollectionMethodLines) {
		createTestCollectionMethod(
			11,
			testCollectionMethodCallLines,
			testCollectionMethodLines,
			(successfulTestLines, testWithErrorLines) -> addNumericOperatorTests("+", successfulTestLines, testWithErrorLines),
			(successfulTestLines, testWithErrorLines) -> addNumericOperatorTests("-", successfulTestLines, testWithErrorLines),
			(successfulTestLines, testWithErrorLines) -> addStringConcatenationTests(successfulTestLines)
		);
	}

	private static void addOperator10Tests(List<String> testCollectionMethodCallLines, List<String> testCollectionMethodLines) {
		createTestCollectionMethod(
			10,
			testCollectionMethodCallLines,
			testCollectionMethodLines,
			(successfulTestLines, testWithErrorLines) -> addShiftOperatorTests("<<",  successfulTestLines, testWithErrorLines),
			(successfulTestLines, testWithErrorLines) -> addShiftOperatorTests(">>",  successfulTestLines, testWithErrorLines),
			(successfulTestLines, testWithErrorLines) -> addShiftOperatorTests(">>>", successfulTestLines, testWithErrorLines)
		);
	}

	private static void addOperator9Tests(List<String> testCollectionMethodCallLines, List<String> testCollectionMethodLines) {
		createTestCollectionMethod(
			9,
			testCollectionMethodCallLines,
			testCollectionMethodLines,
			(successfulTestLines, testWithErrorLines) -> addNumericOperatorTests("<", successfulTestLines, testWithErrorLines),
			(successfulTestLines, testWithErrorLines) -> addNumericOperatorTests("<=", successfulTestLines, testWithErrorLines),
			(successfulTestLines, testWithErrorLines) -> addNumericOperatorTests(">", successfulTestLines, testWithErrorLines),
			(successfulTestLines, testWithErrorLines) -> addNumericOperatorTests(">=", successfulTestLines, testWithErrorLines)
		);
	}

	private static void addOperator8Tests(List<String> testCollectionMethodCallLines, List<String> testCollectionMethodLines) {
		createTestCollectionMethod(
			8,
			testCollectionMethodCallLines,
			testCollectionMethodLines,
			(successfulTestLines, testWithErrorLines) -> addIdentityComparisonOperatorTests("==", successfulTestLines, testWithErrorLines),
			(successfulTestLines, testWithErrorLines) -> addIdentityComparisonOperatorTests("!=", successfulTestLines, testWithErrorLines)
		);
	}

	private static void addOperator7Tests(List<String> testCollectionMethodCallLines, List<String> testCollectionMethodLines) {
		createTestCollectionMethod(
			7,
			testCollectionMethodCallLines,
			testCollectionMethodLines,
			(successfulTestLines, testWithErrorLines) -> addBitOperatorTests("&", successfulTestLines, testWithErrorLines)
		);
	}

	private static void addOperator6Tests(List<String> testCollectionMethodCallLines, List<String> testCollectionMethodLines) {
		createTestCollectionMethod(
			6,
			testCollectionMethodCallLines,
			testCollectionMethodLines,
			(successfulTestLines, testWithErrorLines) -> addBitOperatorTests("^", successfulTestLines, testWithErrorLines)
		);
	}

	private static void addOperator5Tests(List<String> testCollectionMethodCallLines, List<String> testCollectionMethodLines) {
		createTestCollectionMethod(
			5,
			testCollectionMethodCallLines,
			testCollectionMethodLines,
			(successfulTestLines, testWithErrorLines) -> addBitOperatorTests("|", successfulTestLines, testWithErrorLines)
		);
	}

	private static void addOperator4Tests(List<String> testCollectionMethodCallLines, List<String> testCollectionMethodLines) {
		createTestCollectionMethod(
			4,
			testCollectionMethodCallLines,
			testCollectionMethodLines,
			(successfulTestLines, testWithErrorLines) -> addLogicOperatorTests("&&", successfulTestLines, testWithErrorLines)
		);
	}

	private static void addOperator3Tests(List<String> testCollectionMethodCallLines, List<String> testCollectionMethodLines) {
		createTestCollectionMethod(
			3,
			testCollectionMethodCallLines,
			testCollectionMethodLines,
			(successfulTestLines, testWithErrorLines) -> addLogicOperatorTests("||", successfulTestLines, testWithErrorLines)
		);
	}

	private static void addOperator1Tests(List<String> testCollectionMethodCallLines, List<String> testCollectionMethodLines) {
		createTestCollectionMethod(
			1,
			testCollectionMethodCallLines,
			testCollectionMethodLines,
			(successfulTestLines, testWithErrorLines) -> addAssignmentOperatorTests(successfulTestLines, testWithErrorLines),
			(successfulTestLines, testWithErrorLines) -> addNumericOperatorWithAssignmentTests("+=", successfulTestLines, testWithErrorLines),
			(successfulTestLines, testWithErrorLines) -> addNumericOperatorWithAssignmentTests("-=", successfulTestLines, testWithErrorLines),
			(successfulTestLines, testWithErrorLines) -> addNumericOperatorWithAssignmentTests("*=", successfulTestLines, testWithErrorLines),
			(successfulTestLines, testWithErrorLines) -> addNumericOperatorWithAssignmentTests("/=", successfulTestLines, testWithErrorLines),
			(successfulTestLines, testWithErrorLines) -> addNumericOperatorWithAssignmentTests("%=", successfulTestLines, testWithErrorLines),
			(successfulTestLines, testWithErrorLines) -> addBitOperatorWithAssignmentTests("&=", successfulTestLines, testWithErrorLines),
			(successfulTestLines, testWithErrorLines) -> addBitOperatorWithAssignmentTests("^=", successfulTestLines, testWithErrorLines),
			(successfulTestLines, testWithErrorLines) -> addBitOperatorWithAssignmentTests("|=", successfulTestLines, testWithErrorLines),
			(successfulTestLines, testWithErrorLines) -> addShiftOperatorWithAssignmentTests("<<=", successfulTestLines, testWithErrorLines),
			(successfulTestLines, testWithErrorLines) -> addShiftOperatorWithAssignmentTests(">>=", successfulTestLines, testWithErrorLines),
			(successfulTestLines, testWithErrorLines) -> addShiftOperatorWithAssignmentTests(">>>=", successfulTestLines, testWithErrorLines)
		);
	}

	private static void createTestCollectionMethod(int operatorPrecedenceLevel, List<String> testCollectionMethodCallLines, List<String> testCollectionMethodLines, TestCaseLineProviders... testCaseLineProviders) {
		String methodName = "collectTestDataForOperator" + operatorPrecedenceLevel;

		String testCollectionMethodCallLine = createTestCollectionCallLine(methodName);
		testCollectionMethodCallLines.add(testCollectionMethodCallLine);

		List<String> successfulTestLines = new ArrayList<>();
		List<String> testWithErrorLines = new ArrayList<>();
		for (TestCaseLineProviders testCaseLineProvider : testCaseLineProviders) {
			testCaseLineProvider.addTestLines(successfulTestLines, testWithErrorLines);
		}

		String testCollectionMethodSignatureLine = createTestCollectionSignatureLine(methodName);
		testCollectionMethodLines.add("");
		testCollectionMethodLines.add(testCollectionMethodSignatureLine);
		testCollectionMethodLines.addAll(successfulTestLines);
		testCollectionMethodLines.add("");
		testCollectionMethodLines.addAll(testWithErrorLines);
		testCollectionMethodLines.add("\t}");
	}

	private static void addNumericOperatorTests(String operator, List<String> successfulTestLines, List<String> testWithErrorLines) {
		addTestBuilderLine(successfulTestLines);
		addTestBuilderLine(testWithErrorLines);
		for (Type lhsType : Type.values()) {
			if (lhsType == Type.STRING && "+".equals(operator)) {
				// String concatenations will be handled differently
				continue;
			}
			for (Type rhsType : Type.values()) {
				if (rhsType == Type.STRING && "+".equals(operator)) {
					// String concatenations will be handled differently
					continue;
				}
				String varName1 = lhsType.getVariableName1();
				String varName2 = rhsType.getVariableName2();
				String boxedVarName1 = lhsType.getBoxedVariableName1();
				String boxedVarName2 = rhsType.getBoxedVariableName2();
				if (lhsType.isNumeric() && rhsType.isNumeric()) {
					addSuccessfulTest(operator, varName1, varName2, successfulTestLines);
					addSuccessfulTest(operator, varName1, boxedVarName2, successfulTestLines);
					addSuccessfulTest(operator, boxedVarName1, varName2, successfulTestLines);
					addSuccessfulTest(operator, boxedVarName1, boxedVarName2, successfulTestLines);
				} else {
					addTestWithError(operator, varName1, varName2, testWithErrorLines);
					boolean lhsPrimitive = lhsType.isPrimitive();
					boolean rhsPrimitive = rhsType.isPrimitive();
					if (rhsPrimitive) {
						addTestWithError(operator, varName1, boxedVarName2, testWithErrorLines);
					}
					if (lhsPrimitive) {
						addTestWithError(operator, boxedVarName1, varName2, testWithErrorLines);
					}
					if (lhsPrimitive && rhsPrimitive) {
						addTestWithError(operator, boxedVarName1, boxedVarName2, testWithErrorLines);
					}
				}
			}
		}
		successfulTestLines.add(";\n");
		testWithErrorLines.add(";\n");
	}

	private static void addStringConcatenationTests(List<String> successfulTestLines) {
		addTestBuilderLine(successfulTestLines);
		for (Type lhsType : Type.values()) {
			for (Type rhsType : Type.values()) {
				if (lhsType != Type.STRING && rhsType != Type.STRING) {
					// no String concatenation
					continue;
				}
				String varName1 = lhsType.getVariableName1();
				String varName2 = rhsType.getVariableName2();
				String boxedVarName1 = lhsType.getBoxedVariableName1();
				String boxedVarName2 = rhsType.getBoxedVariableName2();
				boolean lhsPrimitive = lhsType.isPrimitive();
				boolean rhsPrimitive = rhsType.isPrimitive();

				addSuccessfulTest("+", varName1, varName2, successfulTestLines);
				if (rhsPrimitive) {
					addSuccessfulTest("+", varName1, boxedVarName2, successfulTestLines);
				}
				if (lhsPrimitive) {
					addSuccessfulTest("+", boxedVarName1, varName2, successfulTestLines);
				}
			}
		}
		successfulTestLines.add(";\n");
	}

	private static void addShiftOperatorTests(String operator, List<String> successfulTestLines, List<String> testWithErrorLines) {
		addTestBuilderLine(successfulTestLines);
		addTestBuilderLine(testWithErrorLines);
		for (Type lhsType : Type.values()) {
			for (Type rhsType : Type.values()) {
				String varName1 = lhsType.getVariableName1();
				String varName2 = rhsType.getVariableName2();
				String boxedVarName1 = lhsType.getBoxedVariableName1();
				String boxedVarName2 = rhsType.getBoxedVariableName2();
				if (lhsType.isIntegerType() && rhsType.isIntegerType()) {
					addSuccessfulTest(operator, varName1, varName2, successfulTestLines);
					addSuccessfulTest(operator, varName1, boxedVarName2, successfulTestLines);
					addSuccessfulTest(operator, boxedVarName1, varName2, successfulTestLines);
					addSuccessfulTest(operator, boxedVarName1, boxedVarName2, successfulTestLines);
				} else {
					addTestWithError(operator, varName1, varName2, testWithErrorLines);
					boolean lhsPrimitive = lhsType.isPrimitive();
					boolean rhsPrimitive = rhsType.isPrimitive();
					if (rhsPrimitive) {
						addTestWithError(operator, varName1, boxedVarName2, testWithErrorLines);
					}
					if (lhsPrimitive) {
						addTestWithError(operator, boxedVarName1, varName2, testWithErrorLines);
					}
					if (lhsPrimitive && rhsPrimitive) {
						addTestWithError(operator, boxedVarName1, boxedVarName2, testWithErrorLines);
					}
				}
			}
		}
		successfulTestLines.add(";\n");
		testWithErrorLines.add(";\n");
	}

	private static void addIdentityComparisonOperatorTests(String operator, List<String> successfulTestLines, List<String> testWithErrorLines) {
		addTestBuilderLine(successfulTestLines);
		addTestBuilderLine(testWithErrorLines);
		for (Type lhsType : Type.values()) {
			for (Type rhsType : Type.values()) {
				String varName1 = lhsType.getVariableName1();
				String varName2 = rhsType.getVariableName2();
				String boxedVarName1 = lhsType.getBoxedVariableName1();
				String boxedVarName2 = rhsType.getBoxedVariableName2();

				final boolean comparisonUnboxedUnboxedValid;
				final boolean comparisonUnboxedBoxedValid;
				final boolean comparisonBoxedUnboxedValid;
				final boolean comparisonBoxedBoxedValid;
				if (lhsType == rhsType) {
					comparisonUnboxedUnboxedValid = comparisonUnboxedBoxedValid = comparisonBoxedUnboxedValid = comparisonBoxedBoxedValid = true;
				} else {
					if (lhsType == Type.BOOLEAN) {
						comparisonUnboxedUnboxedValid = comparisonUnboxedBoxedValid = false;
						comparisonBoxedUnboxedValid = comparisonBoxedBoxedValid = rhsType == Type.OBJECT;
					} else if (rhsType == Type.BOOLEAN) {
						comparisonUnboxedUnboxedValid = comparisonBoxedUnboxedValid = false;
						comparisonUnboxedBoxedValid = comparisonBoxedBoxedValid = lhsType == Type.OBJECT;
					} else if (lhsType == Type.STRING) {
						comparisonBoxedUnboxedValid = comparisonBoxedBoxedValid = comparisonUnboxedBoxedValid = false;
						comparisonUnboxedUnboxedValid = rhsType == Type.OBJECT;
					} else if (rhsType == Type.STRING) {
						comparisonUnboxedBoxedValid = comparisonBoxedBoxedValid = comparisonBoxedUnboxedValid = false;
						comparisonUnboxedUnboxedValid = lhsType == Type.OBJECT;
					} else if (lhsType.isNumeric()) {
						comparisonUnboxedUnboxedValid = comparisonUnboxedBoxedValid = rhsType.isNumeric();
						comparisonBoxedUnboxedValid = rhsType.isNumeric() || rhsType == Type.OBJECT;
						comparisonBoxedBoxedValid = false;
					} else {
						if (lhsType != Type.OBJECT) {
							throw new IllegalStateException("Unexpected state: lhs type is " + lhsType + ". The case distinction does not seem to be complete.");
						}
						comparisonUnboxedUnboxedValid = comparisonBoxedUnboxedValid = false;
						comparisonUnboxedBoxedValid = true;
						comparisonBoxedBoxedValid = false;
					}
				}

				boolean lhsPrimitive = lhsType.isPrimitive();
				boolean rhsPrimitive = rhsType.isPrimitive();
				addSuccessfulTestOrTestWithError(operator, varName1, varName2, successfulTestLines, testWithErrorLines, comparisonUnboxedUnboxedValid);
				if (rhsPrimitive) {
					addSuccessfulTestOrTestWithError(operator, varName1, boxedVarName2, successfulTestLines, testWithErrorLines, comparisonUnboxedBoxedValid);
				}
				if (lhsPrimitive) {
					addSuccessfulTestOrTestWithError(operator, boxedVarName1, varName2, successfulTestLines, testWithErrorLines, comparisonBoxedUnboxedValid);
				}
				if (lhsPrimitive && rhsPrimitive) {
					addSuccessfulTestOrTestWithError(operator, boxedVarName1, boxedVarName2, successfulTestLines, testWithErrorLines, comparisonBoxedBoxedValid);
				}
			}
		}
		successfulTestLines.add(";\n");
		testWithErrorLines.add(";\n");
	}

	private static void addBitOperatorTests(String operator, List<String> successfulTestLines, List<String> testWithErrorLines) {
		addTestBuilderLine(successfulTestLines);
		addTestBuilderLine(testWithErrorLines);
		for (Type lhsType : Type.values()) {
			for (Type rhsType : Type.values()) {
				String varName1 = lhsType.getVariableName1();
				String varName2 = rhsType.getVariableName2();
				String boxedVarName1 = lhsType.getBoxedVariableName1();
				String boxedVarName2 = rhsType.getBoxedVariableName2();
				boolean comparisonValid = lhsType == Type.BOOLEAN && rhsType == Type.BOOLEAN || lhsType.isIntegerType() && rhsType.isIntegerType();
				if (comparisonValid) {
					addSuccessfulTest(operator, varName1, varName2, successfulTestLines);
					addSuccessfulTest(operator, varName1, boxedVarName2, successfulTestLines);
					addSuccessfulTest(operator, boxedVarName1, varName2, successfulTestLines);
					addSuccessfulTest(operator, boxedVarName1, boxedVarName2, successfulTestLines);
				} else {
					boolean lhsPrimitive = lhsType.isPrimitive();
					boolean rhsPrimitive = rhsType.isPrimitive();
					addTestWithError(operator, varName1, varName2, testWithErrorLines);
					if (rhsPrimitive) {
						addTestWithError(operator, varName1, boxedVarName2, testWithErrorLines);
					}
					if (lhsPrimitive) {
						addTestWithError(operator, boxedVarName1, varName2, testWithErrorLines);
					}
					if (lhsPrimitive && rhsPrimitive) {
						addTestWithError(operator, boxedVarName1, boxedVarName2, testWithErrorLines);
					}
				}
			}
		}
		successfulTestLines.add(";\n");
		testWithErrorLines.add(";\n");
	}

	private static void addLogicOperatorTests(String operator, List<String> successfulTestLines, List<String> testWithErrorLines) {
		addTestBuilderLine(successfulTestLines);
		addTestBuilderLine(testWithErrorLines);
		for (Type lhsType : Type.values()) {
			for (Type rhsType : Type.values()) {
				String varName1 = lhsType.getVariableName1();
				String varName2 = rhsType.getVariableName2();
				String boxedVarName1 = lhsType.getBoxedVariableName1();
				String boxedVarName2 = rhsType.getBoxedVariableName2();
				boolean operatorValid = lhsType == Type.BOOLEAN && rhsType == Type.BOOLEAN;
				if (operatorValid) {
					addSuccessfulTest(operator, varName1, varName2, successfulTestLines);
					addSuccessfulTest(operator, varName1, boxedVarName2, successfulTestLines);
					addSuccessfulTest(operator, boxedVarName1, varName2, successfulTestLines);
					addSuccessfulTest(operator, boxedVarName1, boxedVarName2, successfulTestLines);
				} else {
					boolean lhsPrimitive = lhsType.isPrimitive();
					boolean rhsPrimitive = rhsType.isPrimitive();
					addTestWithError(operator, varName1, varName2, testWithErrorLines);
					if (rhsPrimitive) {
						addTestWithError(operator, varName1, boxedVarName2, testWithErrorLines);
					}
					if (lhsPrimitive) {
						addTestWithError(operator, boxedVarName1, varName2, testWithErrorLines);
					}
					if (lhsPrimitive && rhsPrimitive) {
						addTestWithError(operator, boxedVarName1, boxedVarName2, testWithErrorLines);
					}
				}
			}
		}
		successfulTestLines.add(";\n");
		testWithErrorLines.add(";\n");
	}

	private static void addAssignmentOperatorTests(List<String> successfulTestLines, List<String> testWithErrorLines) {
		addTestBuilderLine(successfulTestLines);
		addTestBuilderLine(testWithErrorLines);
		for (Type lhsType : Type.values()) {
			for (Type rhsType : Type.values()) {
				String varNameForAssignment = lhsType.getVariableNameForAssignment();
				String varName2 = rhsType.getVariableName2();
				String boxedVarNameForAssignment = lhsType.getBoxedVariableNameForAssignment();
				String boxedVarName2 = rhsType.getBoxedVariableName2();
				boolean lhsPrimitive = lhsType.isPrimitive();
				boolean rhsPrimitive = rhsType.isPrimitive();

				boolean assignmentUnboxedUnboxedValid = lhsType.isAssignableFrom(rhsType);
				boolean assignmentUnboxedBoxedValid = assignmentUnboxedUnboxedValid;
				boolean assignmentBoxedUnboxedValid = lhsType == rhsType;
				boolean assignmentBoxedBoxedValid = assignmentBoxedUnboxedValid;
				addSuccessfulTestOrTestWithError("=", varNameForAssignment, varName2, successfulTestLines, testWithErrorLines, assignmentUnboxedUnboxedValid);
				if (rhsPrimitive) {
					addSuccessfulTestOrTestWithError("=", varNameForAssignment, boxedVarName2, successfulTestLines, testWithErrorLines, assignmentUnboxedBoxedValid);
				}
				if (lhsPrimitive) {
					addSuccessfulTestOrTestWithError("=", boxedVarNameForAssignment, varName2, successfulTestLines, testWithErrorLines, assignmentBoxedUnboxedValid);
				}
				if (lhsPrimitive && rhsPrimitive) {
					addSuccessfulTestOrTestWithError("=", boxedVarNameForAssignment, boxedVarName2, successfulTestLines, testWithErrorLines, assignmentBoxedBoxedValid);
				}
			}
		}
		successfulTestLines.add(";\n");
		testWithErrorLines.add(";\n");
	}

	private static void addNumericOperatorWithAssignmentTests(String operator, List<String> successfulTestLines, List<String> testWithErrorLines) {
		addTestBuilderLine(successfulTestLines);
		addTestBuilderLine(testWithErrorLines);
		for (Type lhsType : Type.values()) {
			if (lhsType == Type.STRING && "+=".equals(operator)) {
				// String concatenations will be handled differently
				continue;
			}
			for (Type rhsType : Type.values()) {
				String varNameForAssignment = lhsType.getVariableNameForAssignment();
				String varName2 = rhsType.getVariableName2();
				String boxedVarNameForAssignment = lhsType.getBoxedVariableNameForAssignment();
				String boxedVarName2 = rhsType.getBoxedVariableName2();
				boolean lhsPrimitive = lhsType.isPrimitive();
				boolean rhsPrimitive = rhsType.isPrimitive();

				boolean assignmentUnboxedUnboxedValid = lhsType.isNumeric() && rhsType.isNumeric();
				boolean assignmentUnboxedBoxedValid = assignmentUnboxedUnboxedValid;
				boolean assignmentBoxedUnboxedValid = lhsType.isAssignableFrom(Type.INT) && lhsType.isAssignableFrom(rhsType);
				boolean assignmentBoxedBoxedValid = assignmentBoxedUnboxedValid;
				addSuccessfulTestOrTestWithError(operator, varNameForAssignment, varName2, successfulTestLines, testWithErrorLines, assignmentUnboxedUnboxedValid);
				if (rhsPrimitive) {
					addSuccessfulTestOrTestWithError(operator, varNameForAssignment, boxedVarName2, successfulTestLines, testWithErrorLines, assignmentUnboxedBoxedValid);
				}
				if (lhsPrimitive) {
					addSuccessfulTestOrTestWithError(operator, boxedVarNameForAssignment, varName2, successfulTestLines, testWithErrorLines, assignmentBoxedUnboxedValid);
				}
				if (lhsPrimitive && rhsPrimitive) {
					addSuccessfulTestOrTestWithError(operator, boxedVarNameForAssignment, boxedVarName2, successfulTestLines, testWithErrorLines, assignmentBoxedBoxedValid);
				}
			}
		}
		successfulTestLines.add(";\n");
		testWithErrorLines.add(";\n");
	}

	private static void addBitOperatorWithAssignmentTests(String operator, List<String> successfulTestLines, List<String> testWithErrorLines) {
		// TODO
	}

	private static void addShiftOperatorWithAssignmentTests(String operator, List<String> successfulTestLines, List<String> testWithErrorLines) {
		// TODO
	}

	private static String createTestCollectionCallLine(String methodName) {
		return "\t\t" + methodName + "(" + TEST_BUILDER_NAME + ", " + TEST_INSTANCE_NAME + ");";
	}

	private static String createTestCollectionSignatureLine(String methodName) {
		return "\tprivate static void " + methodName + "(EvaluationTestBuilder " + TEST_BUILDER_NAME + ", " + TEST_INSTANCE_CLASS_NAME + " " + TEST_INSTANCE_NAME + ") {";
	}

	private static void addTestBuilderLine(List<String> lines) {
		String testBuilderLine = createTestBuilderLine();
		lines.add(testBuilderLine);
	}

	private static String createTestBuilderLine() {
		return "\t\t" + TEST_BUILDER_NAME;
	}

	private static void addSuccessfulTestOrTestWithError(String operator, String lhsVariableName, String rhsVariableName, List<String> successfulTestLines, List<String> testWithErrorLines, boolean success) {
		if (success) {
			addSuccessfulTest(operator, lhsVariableName, rhsVariableName, successfulTestLines);
		} else {
			addTestWithError(operator, lhsVariableName, rhsVariableName, testWithErrorLines);
		}
	}

	private static void addSuccessfulTest(String operator, String lhsVariableName, String rhsVariableName, List<String> successfulTestLines) {
		String testLine = createSuccessfulTestLine(operator, lhsVariableName, rhsVariableName);
		successfulTestLines.add(testLine);
	}

	private static String createSuccessfulTestLine(String operator, String lhsVariableName, String rhsVariableName) {
		return "\t\t\t.addTest(\"" + lhsVariableName + " " + operator + " " + rhsVariableName + "\",\t" + TEST_INSTANCE_NAME + "." + lhsVariableName + " " + operator + " " + TEST_INSTANCE_NAME + "." + rhsVariableName + ")";
	}

	private static void addTestWithError(String operator, String lhsVariableName, String rhsVariableName, List<String> testWithErrorLines) {
		String testLine = createTestWithErrorLine(operator, lhsVariableName, rhsVariableName);
		testWithErrorLines.add(testLine);
	}

	private static String createTestWithErrorLine(String operator, String lhsVariableName, String rhsVariableName) {
		return "\t\t\t.addTestWithError(\"" + lhsVariableName + " " + operator + " " + rhsVariableName + "\",\tSemanticException.class)";
	}

	private static String createTestCode(String testCollectionMethodCalls, String testCollectionMethods) throws IOException {
		String testCodeTemplate = loadTestCodeTemplate();
		return testCodeTemplate
			.replace("$TEST_PACKAGE$",					BinaryOperatorTestAllPermutationsGenerator.class.getPackage().getName())
			.replace("$CLASS_NAME$",					TEST_CLASS_NAME)
			.replace("$TEST_BUILDER_NAME$",				TEST_BUILDER_NAME)
			.replace("$TEST_INSTANCE_CLASS$",			TEST_INSTANCE_CLASS_NAME)
			.replace("$TEST_INSTANCE_NAME$",			TEST_INSTANCE_NAME)
			.replace("$TEST_COLLECTION_METHOD_CALLS$",	testCollectionMethodCalls)
			.replace("$TEST_COLLECTION_METHODS$",		testCollectionMethods)
			.replace("$BOOLEAN_VARIABLE_1$",			Type.BOOLEAN.getVariableName1())
			.replace("$BOOLEAN_VARIABLE_2$",			Type.BOOLEAN.getVariableName2())
			.replace("$CHAR_VARIABLE_1$",				Type.CHAR.getVariableName1())
			.replace("$CHAR_VARIABLE_2$",				Type.CHAR.getVariableName2())
			.replace("$BYTE_VARIABLE_1$",				Type.BYTE.getVariableName1())
			.replace("$BYTE_VARIABLE_2$",				Type.BYTE.getVariableName2())
			.replace("$SHORT_VARIABLE_1$",				Type.SHORT.getVariableName1())
			.replace("$SHORT_VARIABLE_2$",				Type.SHORT.getVariableName2())
			.replace("$INT_VARIABLE_1$",				Type.INT.getVariableName1())
			.replace("$INT_VARIABLE_2$",				Type.INT.getVariableName2())
			.replace("$LONG_VARIABLE_1$",				Type.LONG.getVariableName1())
			.replace("$LONG_VARIABLE_2$",				Type.LONG.getVariableName2())
			.replace("$FLOAT_VARIABLE_1$",				Type.FLOAT.getVariableName1())
			.replace("$FLOAT_VARIABLE_2$",				Type.FLOAT.getVariableName2())
			.replace("$DOUBLE_VARIABLE_1$",				Type.DOUBLE.getVariableName1())
			.replace("$DOUBLE_VARIABLE_2$",				Type.DOUBLE.getVariableName2())
			.replace("$BOOLEAN_VARIABLE_BOXED_1$",		Type.BOOLEAN.getBoxedVariableName1())
			.replace("$BOOLEAN_VARIABLE_BOXED_2$",		Type.BOOLEAN.getBoxedVariableName2())
			.replace("$CHAR_VARIABLE_BOXED_1$",			Type.CHAR.getBoxedVariableName1())
			.replace("$CHAR_VARIABLE_BOXED_2$",			Type.CHAR.getBoxedVariableName2())
			.replace("$BYTE_VARIABLE_BOXED_1$",			Type.BYTE.getBoxedVariableName1())
			.replace("$BYTE_VARIABLE_BOXED_2$",			Type.BYTE.getBoxedVariableName2())
			.replace("$SHORT_VARIABLE_BOXED_1$",		Type.SHORT.getBoxedVariableName1())
			.replace("$SHORT_VARIABLE_BOXED_2$",		Type.SHORT.getBoxedVariableName2())
			.replace("$INT_VARIABLE_BOXED_1$",			Type.INT.getBoxedVariableName1())
			.replace("$INT_VARIABLE_BOXED_2$",			Type.INT.getBoxedVariableName2())
			.replace("$LONG_VARIABLE_BOXED_1$",			Type.LONG.getBoxedVariableName1())
			.replace("$LONG_VARIABLE_BOXED_2$",			Type.LONG.getBoxedVariableName2())
			.replace("$FLOAT_VARIABLE_BOXED_1$",		Type.FLOAT.getBoxedVariableName1())
			.replace("$FLOAT_VARIABLE_BOXED_2$",		Type.FLOAT.getBoxedVariableName2())
			.replace("$DOUBLE_VARIABLE_BOXED_1$",		Type.DOUBLE.getBoxedVariableName1())
			.replace("$DOUBLE_VARIABLE_BOXED_2$",		Type.DOUBLE.getBoxedVariableName2())
			.replace("$STRING_VARIABLE_1$",				Type.STRING.getVariableName1())
			.replace("$STRING_VARIABLE_2$",				Type.STRING.getVariableName2())
			.replace("$OBJECT_VARIABLE_1$",				Type.OBJECT.getVariableName1())
			.replace("$OBJECT_VARIABLE_2$",				Type.OBJECT.getVariableName2())
			.replace("$BOOLEAN_VARIABLE_FOR_ASSIGNMENT$",			Type.BOOLEAN.getVariableNameForAssignment())
			.replace("$CHAR_VARIABLE_FOR_ASSIGNMENT$",				Type.CHAR.getVariableNameForAssignment())
			.replace("$BYTE_VARIABLE_FOR_ASSIGNMENT$",				Type.BYTE.getVariableNameForAssignment())
			.replace("$SHORT_VARIABLE_FOR_ASSIGNMENT$",				Type.SHORT.getVariableNameForAssignment())
			.replace("$INT_VARIABLE_FOR_ASSIGNMENT$",				Type.INT.getVariableNameForAssignment())
			.replace("$LONG_VARIABLE_FOR_ASSIGNMENT$",				Type.LONG.getVariableNameForAssignment())
			.replace("$FLOAT_VARIABLE_FOR_ASSIGNMENT$",				Type.FLOAT.getVariableNameForAssignment())
			.replace("$DOUBLE_VARIABLE_FOR_ASSIGNMENT$",				Type.DOUBLE.getVariableNameForAssignment())
			.replace("$BOOLEAN_VARIABLE_BOXED_FOR_ASSIGNMENT$",		Type.BOOLEAN.getBoxedVariableNameForAssignment())
			.replace("$CHAR_VARIABLE_BOXED_FOR_ASSIGNMENT$",			Type.CHAR.getBoxedVariableNameForAssignment())
			.replace("$BYTE_VARIABLE_BOXED_FOR_ASSIGNMENT$",			Type.BYTE.getBoxedVariableNameForAssignment())
			.replace("$SHORT_VARIABLE_BOXED_FOR_ASSIGNMENT$",		Type.SHORT.getBoxedVariableNameForAssignment())
			.replace("$INT_VARIABLE_BOXED_FOR_ASSIGNMENT$",			Type.INT.getBoxedVariableNameForAssignment())
			.replace("$LONG_VARIABLE_BOXED_FOR_ASSIGNMENT$",			Type.LONG.getBoxedVariableNameForAssignment())
			.replace("$FLOAT_VARIABLE_BOXED_FOR_ASSIGNMENT$",		Type.FLOAT.getBoxedVariableNameForAssignment())
			.replace("$DOUBLE_VARIABLE_BOXED_FOR_ASSIGNMENT$",		Type.DOUBLE.getBoxedVariableNameForAssignment())
			.replace("$STRING_VARIABLE_FOR_ASSIGNMENT$",				Type.STRING.getVariableNameForAssignment())
			.replace("$OBJECT_VARIABLE_FOR_ASSIGNMENT$",				Type.OBJECT.getVariableNameForAssignment());
	}

	private static String loadTestCodeTemplate() throws IOException {
		URL resourceDirectoryUrl = BinaryOperatorTestAllPermutationsGenerator.class.getResource("/");
		URI resourceDirectoryUri;
		try {
			resourceDirectoryUri = resourceDirectoryUrl.toURI();
		} catch (URISyntaxException e) {
			throw new IllegalStateException("Resource URL '" + resourceDirectoryUrl + "' is not valid URI");
		}
		Path resourceDirectory = Paths.get(resourceDirectoryUri);
		Path testCodeTemplateFile = resourceDirectory.resolve(TEST_CODE_TEMPLATE_FILE_NAME);
		return new String(Files.readAllBytes(testCodeTemplateFile));
	}

	@FunctionalInterface
	private interface TestCaseLineProviders
	{
		void addTestLines(List<String> successfulTestLines, List<String> testWithErrorLines);
	}

	private enum Type
	{
		BOOLEAN("bool", TypeOfType.LOGIC),
		CHAR("c", TypeOfType.INTEGER_NUMBER),
		BYTE("b", TypeOfType.INTEGER_NUMBER),
		SHORT("s", TypeOfType.INTEGER_NUMBER, Type.BYTE),
		INT("i", TypeOfType.INTEGER_NUMBER, Type.SHORT, Type.CHAR),
		LONG("l", TypeOfType.INTEGER_NUMBER, Type.INT),
		FLOAT("f", TypeOfType.FLOATING_POINT_NUMBER, Type.LONG),
		DOUBLE("d", TypeOfType.FLOATING_POINT_NUMBER, Type.FLOAT),
		STRING("str", TypeOfType.TEXT),
		OBJECT("o", TypeOfType.OBJECT, Type.BOOLEAN, Type.DOUBLE, Type.STRING)
		;

		private final String variableNamePrefix;
		private final TypeOfType type;
		private final Type[] assignableTypes;

		Type(String variableNamePrefix, TypeOfType type, Type... assignableTypes) {
			this.variableNamePrefix = variableNamePrefix;
			this.type = type;
			this.assignableTypes = assignableTypes;
		}

		String getVariableName1() {
			return variableNamePrefix + "1";
		}

		String getVariableName2() {
			return variableNamePrefix + "2";
		}

		String getVariableNameForAssignment() {
			return variableNamePrefix;
		}

		String getBoxedVariableName1() {
			return getVariableName1().toUpperCase();
		}

		String getBoxedVariableName2() {
			return getVariableName2().toUpperCase();
		}

		String getBoxedVariableNameForAssignment() {
			return getVariableNameForAssignment().toUpperCase();
		}

		boolean isIntegerType() {
			return type == TypeOfType.INTEGER_NUMBER;
		}

		boolean isNumeric() {
			switch (type) {
				case INTEGER_NUMBER:
				case FLOATING_POINT_NUMBER:
					return true;
				case LOGIC:
				case TEXT:
				case OBJECT:
					return false;
				default:
					throw new IllegalStateException("Unexpected type: " + type);
			}
		}

		boolean isPrimitive() {
			switch (type) {
				case LOGIC:
				case INTEGER_NUMBER:
				case FLOATING_POINT_NUMBER:
					return true;
				case TEXT:
				case OBJECT:
					return false;
				default:
					throw new IllegalStateException("Unexpected type: " + type);
			}
		}

		boolean isAssignableFrom(Type type) {
			if (this == type) {
				return true;
			}
			return Arrays.stream(assignableTypes).anyMatch(assignableType -> assignableType.isAssignableFrom(type));
		}
	}

	private enum TypeOfType
	{
		LOGIC,
		INTEGER_NUMBER,
		FLOATING_POINT_NUMBER,
		TEXT,
		OBJECT
	}
}

package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.api.common.*;
import dd.kms.zenodot.framework.common.ObjectInfoProvider;
import dd.kms.zenodot.framework.wrappers.FieldInfo;
import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaState;
import dd.kms.zenodotx.java.ParserSettings;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;

import java.util.List;

public class FieldRule implements SimpleRule<JavaState>
{
	private final boolean isStatic;

	public FieldRule(boolean isStatic) {
		this.isStatic = isStatic;
	}

	@Override
	public SyntaxRule getSyntaxRule() {
		return () -> JavaRuleSetUtils.IDENTIFIER_PATTERN;
	}

	@Override
	public SemanticRule<JavaState> getSemanticRule() {
		return new AbstractSemanticJavaRule() {
			@Override
			void doSuggestCodeCompletions(String parsedString, JavaState state) {
				// TODO
			}

			@Override
			void doSuggestMethodParameters(JavaState state) {
				// TODO
			}

			@Override
			public void evaluate(String fieldName, JavaState state) throws SemanticException, EvaluationException {
				ParserSettings parserSettings = state.getParserSettings();

				Object context = state.pop();
				ObjectInfoProvider objectInfoProvider = new ObjectInfoProvider(parserSettings.getEvaluationMode());
				Class<?> contextType;
				Object contextObject;
				if (isStatic) {
					contextType = (Class<?>) context;
					contextObject = null;
				} else {
					ObjectInfo contextInfo = (ObjectInfo) context;
					contextType = objectInfoProvider.getType(contextInfo);
					contextObject = contextInfo.getObject();
				}
				FieldScanner fieldScanner = getFieldScanner(fieldName, parserSettings.getMinimumFieldAccessModifier());
				List<FieldInfo> fieldInfos = InfoProvider.getFieldInfos(contextType, fieldScanner);
				if (fieldInfos.isEmpty()) {
					fieldScanner = getFieldScanner(fieldName, AccessModifier.PRIVATE);
					fieldInfos = InfoProvider.getFieldInfos(contextType, fieldScanner);
					throw fieldInfos.isEmpty()
						? new SemanticException("Unknown field '" + fieldName + "'")
						: new SemanticException("Field '" + fieldName + "' is not visible");
				} else if (fieldInfos.size() > 1) {
					throw new SemanticException("Ambiguous field name '" + fieldName + "'");
				}
				FieldInfo fieldInfo = fieldInfos.get(0);
				ObjectInfo fieldValueInfo;
				try {
					fieldValueInfo = objectInfoProvider.getFieldValueInfo(contextObject, fieldInfo);
				} catch (AccessDeniedException e) {
					throw new EvaluationException("Cannot access field 'i': " + e.getMessage());
				}
				state.push(fieldValueInfo);
			}

			private FieldScanner getFieldScanner(String name, AccessModifier minimumAccessModifier) {
				FieldScannerBuilder builder = getFieldScannerBuilder(minimumAccessModifier).name(name);
				return builder.build();
			}

			private FieldScannerBuilder getFieldScannerBuilder(AccessModifier minimumAccessModifier) {
				StaticMode staticMode = isStatic ? StaticMode.STATIC : StaticMode.BOTH;
				return FieldScannerBuilder.create()
					.staticMode(staticMode)
					.minimumAccessModifier(minimumAccessModifier);
			}
		};
	}

	@Override
	public String toString() {
		return isStatic ? "Static field" : "Field";
	}
}

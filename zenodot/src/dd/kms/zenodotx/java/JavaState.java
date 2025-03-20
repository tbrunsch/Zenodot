package dd.kms.zenodotx.java;

import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.stack.Stack;
import dd.kms.zenodotx.state.AbstractState;

import java.util.List;

public class JavaState extends AbstractState<JavaState>
{
	private final ObjectInfo			thisInfo;

	private ParserSettings				parserSettings		= new ParserSettings(EvaluationMode.MIXED, AccessModifier.PUBLIC);

	private Stack<Object> 				evaluationStack		= new Stack<>();
	private final Stack<Stack<Object>>	evaluationStacks	= new Stack<>();

	public JavaState(Object thisValue) {
		this.thisInfo = InfoProvider.createObjectInfo(thisValue);
	}

	public ParserSettings getParserSettings() {
		return parserSettings;
	}

	public void setParserSettings(ParserSettings parserSettings) {
		this.parserSettings = parserSettings;
	}

	public Object pop() {
		return evaluationStack.pop();
	}

	public void push(Object object) {
		evaluationStack.push(object);
	}

	public void pushThis() {
		push(thisInfo);
	}

	@Override
	protected void doStore() {
		evaluationStacks.push(evaluationStack.copy());
	}

	@Override
	protected void doRestore() {
		evaluationStack = evaluationStacks.pop();
	}

	public Object getEvaluatedObject() {
		List<Object> evaluatedObjects = evaluationStack.asList();
		if (evaluatedObjects.isEmpty()) {
			throw new IllegalStateException("The evaluation stack is empty");
		} else if (evaluatedObjects.size() > 1) {
			throw new IllegalStateException("The evaluation stack contains more than one element");
		}
		return evaluatedObjects.get(0);
	}
}

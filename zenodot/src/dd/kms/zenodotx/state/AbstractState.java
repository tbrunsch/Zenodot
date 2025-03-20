package dd.kms.zenodotx.state;

import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.rule.PreparsedGrammar;
import dd.kms.zenodotx.rule.simple.SemanticRule;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public abstract class AbstractState<S extends State<S>> implements State<S>
{
	private final List<EvaluationData<S>> 	evaluationDataList			= new ArrayList<>();
	private final Deque<Integer>			lastEvaluationDataListSizes	= new ArrayDeque<>();

	protected abstract void doStore();
	protected abstract void doRestore();

	@Override
	public void store() {
		lastEvaluationDataListSizes.push(evaluationDataList.size());
		doStore();
	}

	@Override
	public void restore() {
		if (lastEvaluationDataListSizes.isEmpty()) {
			throw new IllegalStateException("Called restore() more often than store()");
		}
		int lastEvaluationDataListSize = lastEvaluationDataListSizes.pop();
		if (evaluationDataList.size() < lastEvaluationDataListSize) {
			throw new IllegalStateException("The list of evaluation data shrunk since the matching call of store()");
		}
		evaluationDataList.subList(lastEvaluationDataListSize, evaluationDataList.size()).clear();
		doRestore();
	}

	@Override
	public final void evaluate(SemanticRule<S> rule, String text) throws EvaluationException, SemanticException {
		EvaluationData<S> evaluationData = new EvaluationData<>(rule, text);
		evaluationData.evaluate((S) this);
		evaluationDataList.add(evaluationData);
	}

	@Override
	public PreparsedGrammar<S> getPreparsedGrammar() {
		ArrayList<EvaluationData<S>> rules = new ArrayList<>(evaluationDataList);
		return state -> {
			for (EvaluationData<S> rule : rules) {
				rule.evaluate(state);
			}
		};
	}

	private static class EvaluationData<S extends State<S>>
	{
		final SemanticRule<S>	rule;
		final String			text;

		EvaluationData(SemanticRule<S> rule, String text) {
			this.rule = rule;
			this.text = text;
		}

		void evaluate(S state) throws EvaluationException, SemanticException {
			rule.evaluate(text, state);
		}
	}
}

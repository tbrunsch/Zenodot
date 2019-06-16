package dd.kms.zenodot;

/**
 * Compiler for expressions<br/>
 * <br/>
 * In contrast to the {@link ExpressionParser}, the compiler returns a {@link CompiledExpression}
 * instead of an object. This parse result allows a faster evaluation of the same expression for different
 * values of {@code this} compared to evaluating the expression for each of these values individually.
 */
public interface ExpressionCompiler
{
	 CompiledExpression compile() throws ParseException;
}

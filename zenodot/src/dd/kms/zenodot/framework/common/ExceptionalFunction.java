package dd.kms.zenodot.framework.common;

@FunctionalInterface
public interface ExceptionalFunction<T, R>
{
	R apply(T t) throws Exception;
}

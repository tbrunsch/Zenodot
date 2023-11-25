package dd.kms.zenodot.impl.directories;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

class CacheUtils
{
	static <K, V> ExceptionalFunction<K, V> cacheDelegate(ExceptionalFunction<K, V> functionToCache, long timeUntilEvictionMs) {
		LoadingCache<K, V> cache = CacheBuilder.newBuilder()
			.expireAfterWrite(timeUntilEvictionMs, TimeUnit.MILLISECONDS)
			.build(new CacheLoader<K, V>() {
				@Override
				public V load(K key) throws Exception {
					return functionToCache.apply(key);
				}
			});
		return key -> {
			try {
				return cache.get(key);
			} catch (ExecutionException e) {
				Throwable cause = e.getCause();
				Throwables.throwIfUnchecked(cause);
				throw (IOException) cause;
			}
		};
	}

	static <K1, K2, V> ExceptionalBiFunction<K1, K2, V> cacheDelegate(ExceptionalBiFunction<K1, K2, V> biFunctionToCache, long timeUntilEvictionMs) {
		ExceptionalFunction<Pair<K1, K2>, V> functionToCache = p -> biFunctionToCache.apply(p.getFirst(), p.getSecond());
		ExceptionalFunction<Pair<K1, K2>, V> cachedFunction = cacheDelegate(functionToCache, timeUntilEvictionMs);
		return (k1, k2) -> cachedFunction.apply(new Pair<>(k1, k2));
	}

	@FunctionalInterface
	interface ExceptionalFunction<K, V>
	{
		V apply(K key) throws IOException;
	}

	@FunctionalInterface
	interface ExceptionalBiFunction<K1, K2, V>
	{
		V apply(K1 key1, K2 key2) throws IOException;
	}

	private static class Pair<U, V>
	{
		private final U first;
		private final V second;

		Pair(U first, V second) {
			this.first = first;
			this.second = second;
		}

		U getFirst() {
			return first;
		}

		V getSecond() {
			return second;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Pair<?, ?> pair = (Pair<?, ?>) o;
			return Objects.equals(first, pair.first) &&
				Objects.equals(second, pair.second);
		}

		@Override
		public int hashCode() {
			return Objects.hash(first, second);
		}
	}
}

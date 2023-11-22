package dd.kms.zenodot.api.directories;

import com.google.common.cache.CacheBuilder;

public interface CacheConfigurator
{
	<K, V> CacheBuilder<K, V> configure(CacheBuilder<K, V> cacheBuilder);
}

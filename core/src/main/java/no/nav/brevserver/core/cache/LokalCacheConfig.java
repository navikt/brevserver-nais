package no.nav.brevserver.core.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class LokalCacheConfig {
	public static final String SYSTEM_TILGANG_CACHE = "systemTilgang";
	public static final String HENT_SYSTEM_TILGANG_CACHE = "hentTilgang";
	public static final String OIDC_TOKEN_CACHE = "OidcTokenCache";

	@Bean
	@Primary
	@Profile({"nais", "local"})
	CacheManager cacheManager() {
		SimpleCacheManager manager = new SimpleCacheManager();
		manager.setCaches(Arrays.asList(
				new CaffeineCache(SYSTEM_TILGANG_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(10)
						.build()),
				new CaffeineCache(HENT_SYSTEM_TILGANG_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(10)
						.build()),
				new CaffeineCache(OIDC_TOKEN_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(55, TimeUnit.MINUTES)
						.build())
		));
		return manager;
	}
}
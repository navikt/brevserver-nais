package no.nav.brevserver.core;

import no.nav.brevserver.core.cache.LokalCacheConfig;
import no.nav.brevserver.core.repository.RepositoryConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({RepositoryConfig.class, LokalCacheConfig.class})
@Configuration
public class CoreConfig {
}

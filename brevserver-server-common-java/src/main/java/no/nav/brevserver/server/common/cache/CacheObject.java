package no.nav.brevserver.server.common.cache;

/**
 * @author Rune Røren, Accenture
 */
public class CacheObject {

	long timeCreated = 0;
	long lifetime = CacheManager.CACHE_FOREVER;
	Object object = null;

	public CacheObject(Object o) {
		this.object = o;
		this.timeCreated = System.currentTimeMillis();
	}

	public CacheObject(Object o, long lifetime) {
		this.object = o;
		this.lifetime = lifetime;
		this.timeCreated = System.currentTimeMillis();
	}


	public boolean isStillActive() {
		if (lifetime == CacheManager.CACHE_FOREVER) {
			return true;
		}

		if (lifetime == CacheManager.DO_NOT_CACHE) {
			return false;
		}

		if (System.currentTimeMillis() - timeCreated > lifetime) {
			return false;
		}

		return true;
	}
}

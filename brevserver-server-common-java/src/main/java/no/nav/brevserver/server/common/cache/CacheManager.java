package no.nav.brevserver.server.common.cache;

import java.util.HashMap;

import no.nav.brevserver.server.common.config.ConfigManager;

/**
 * Klasse som cacher objekter evig eller i et visst antall ms.
 * 
 * @author Rune Røren, Accenture
 * @version $Revision: 2148 $ $Author: t133126 $ $Date: 2013-07-23 14:24:36 +0200 (ti, 23 jul 2013) $
 */
public class CacheManager {
	public static final int DO_NOT_CACHE = -1;
	public static final int CACHE_FOREVER = 0;
	
	private static HashMap<String, CacheObject> cache = new HashMap<String, CacheObject>(); 

	static int clearCache = ConfigManager.getInstance().getInt("CacheManager.lifetime.ms", CACHE_FOREVER);

	public static Object getObject(String key, long lifetime) {
		if (lifetime == DO_NOT_CACHE) {
			return null;
		}
		return getObject(key);
	}


	public static Object getObject(String key) {
		CacheObject co = (CacheObject) cache.get(key);

		if (co != null) {
			if (co.isStillActive()) {
				return co.object;
			} else {
				cache.remove(key);
			}
		}
		
		return null;
	}

	public static long getAge(String key) {
		CacheObject co = (CacheObject) cache.get(key);

		if (co != null) {
			if (co.isStillActive()) {
				return System.currentTimeMillis() - co.timeCreated;
			}
		}
		return 0;
	}

	
	public static void addObject(final String key, Object object) {
		addObject(key, object, clearCache);
	}

	public static synchronized void addObject(final String key, Object object, long lifetime) {
		if (lifetime == DO_NOT_CACHE) {
			return;
		}
		
		CacheObject co = new CacheObject(object, lifetime);
		cache.put(key, co);
	}
	
	public static void clearCache() {
		cache.clear();
	}
}

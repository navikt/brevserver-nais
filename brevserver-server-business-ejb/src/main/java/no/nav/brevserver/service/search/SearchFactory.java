package no.nav.brevserver.service.search;

/**
 * @author Rune Røren, Accenture
 * @version $Revision: 2148 $ $Author: t133126 $ $Date: 2013-07-23 14:24:36 +0200 (ti, 23 jul 2013) $
 */
public final class SearchFactory {

	private static SearchService instance = null;

	private SearchFactory() {
	}

	public static SearchService getService() {
		if (instance == null) {
			instance = new SearchService();
		}

		return instance;
	}

}

package no.nav.brevserver.server.common.jndi;

import no.nav.brevserver.server.common.exception.BrevRuntimeException;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public enum JndiHelper {

	INSTANCE;

	private final Context context;

	private JndiHelper() {
		try {
			context = new InitialContext();
		} catch (NamingException e) {
			throw new BrevRuntimeException("Instantiation of " + this.getClass().getSimpleName() + " failed" + e.getMessage());
		}
	}

	/**
	 * Convience method.
	 *
	 * @return The INSTANCE of {@link JndiHelper}.
	 */
	public static JndiHelper getInstance() {
		return INSTANCE;
	}

	/**
	 * Gets a bean of the given type using JNDI lookup. The JNDI name used for lookup is "ejblocal:type.getName()".
	 *
	 * @param type The type to lookup
	 * @return An instance of the given type
	 * @throws BrevTechnicalException If the service lookup fails.
	 */
	public <T> T lookup(Class<T> type) throws BrevTechnicalException {
		// WAS default EJB3 JNDI-names are ejblocal:<fully qualified business interface name>
		String jndiName = "ejblocal:" + type.getName();
		return lookup(type, jndiName);
	}

	/**
	 * Gets a bean of the given type using the given string for JNDI lookup.
	 *
	 * @param type     The type to lookup
	 * @param jndiName The JNDI name to lookup
	 * @return An instance of the given type
	 * @throws BrevTechnicalException If the service lookup fails.
	 */
	public <T> T lookup(Class<T> type, String jndiName) throws BrevTechnicalException {
		try {
			return type.cast(context.lookup(jndiName));
		} catch (NamingException e) {
			throw new BrevTechnicalException(BrevTechnicalException.JNDI_OPPSLAG_FEILET,
					"Lookup of " + jndiName + " failed", e);
		} catch (ClassCastException e) {
			throw new BrevTechnicalException(BrevTechnicalException.JNDI_OPPSLAG_FEILET,
					jndiName + " was not of the expected type: " + type.getClass(), e);
		}
	}
}

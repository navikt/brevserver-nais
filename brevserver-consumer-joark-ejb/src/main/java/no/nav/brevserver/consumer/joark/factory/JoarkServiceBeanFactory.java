package no.nav.brevserver.consumer.joark.factory;

import no.nav.brevserver.consumer.joark.JoarkServiceBi;
import no.nav.brevserver.consumer.joark.stub.JoarkServiceStub;
import no.nav.brevserver.server.common.config.ConfigManager;
import no.nav.brevserver.server.common.exception.BrevTechnicalException;
import no.nav.brevserver.server.common.jndi.JndiHelper;

/**
 * Factory for obtaining an instance of {@link JoarkServiceBi}.
 * 
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public enum JoarkServiceBeanFactory {

	INSTANCE;

	private final boolean useStub;

	private JoarkServiceBeanFactory() {
		useStub = ConfigManager.getInstance().getBool("UseMockJOARK", false);
	}

	/**
	 * Convience method.
	 * 
	 * @return The INSTANCE of {@link JoarkServiceBeanFactory}.
	 */
	public static JoarkServiceBeanFactory getInstance() throws BrevTechnicalException {
		return INSTANCE;
	}

	/**
	 * Getter for {@link JoarkServiceBi}.
	 * 
	 * @return An instance of {@link JoarkServiceBi}.
	 * @throws BrevTechnicalException
	 *             If the service lookup fails.
	 */
	public JoarkServiceBi getJoarkService() throws BrevTechnicalException {
		if (useStub) {
			return new JoarkServiceStub();
		} else {
			return JndiHelper.getInstance().lookup(JoarkServiceBi.class);
		}
	}
}